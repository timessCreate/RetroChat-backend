package org.com.timess.retrochat.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Redis分布式锁工具类
 * 基于StringRedisTemplate实现，支持可重入锁、锁续期、防误删等特性
 * @author eternal
 */
@Component
public class RedisDistributedLockUtil {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    // 锁前缀
    private static final String LOCK_PREFIX = "distributed_lock:";
    // 默认锁过期时间（毫秒）
    private static final long DEFAULT_EXPIRE_TIME = 30000L;
    // 默认获取锁等待时间（毫秒）
    private static final long DEFAULT_WAIT_TIME = 5000L;
    // 锁续期检查间隔（毫秒）
    private static final long RENEWAL_INTERVAL = 10000L;
    
    // 存储当前线程持有的锁信息（用于可重入和锁续期）
    private final ThreadLocal<Map<String, LockInfo>> currentLocks = ThreadLocal.withInitial(HashMap::new);
    
    // Lua脚本：释放锁时验证锁标识（原子操作）
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    // Lua脚本：锁续期（原子操作）
    private static final String RENEW_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('pexpire', KEYS[1], ARGV[2]) " +
        "else " +
        "    return 0 " +
        "end";
    
    /**
     * 锁信息类
     */
    private static class LockInfo {
        String requestId;      // 锁标识
        long expireTime;      // 过期时间
        int holdCount;        // 重入次数
        Timer renewalTimer;   // 续期定时器
        
        LockInfo(String requestId, long expireTime) {
            this.requestId = requestId;
            this.expireTime = expireTime;
            this.holdCount = 1;
        }
    }
    
    /**
     * 尝试获取分布式锁（非阻塞）
     * 
     * @param lockKey 锁的key
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_TIME);
    }
    
    /**
     * 尝试获取分布式锁（非阻塞）
     * 
     * @param lockKey 锁的key
     * @param expireTime 锁过期时间（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long expireTime) {
        String fullLockKey = getFullLockKey(lockKey);
        
        // 检查是否已经持有锁（可重入）
        LockInfo lockInfo = getCurrentLockInfo(lockKey);
        if (lockInfo != null) {
            lockInfo.holdCount++;
            return true;
        }
        
        String requestId = UUID.randomUUID().toString();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(fullLockKey, requestId, expireTime, TimeUnit.MILLISECONDS);
        
        if (Boolean.TRUE.equals(success)) {
            // 保存锁信息到ThreadLocal
            LockInfo newLockInfo = new LockInfo(requestId, expireTime);
            currentLocks.get().put(lockKey, newLockInfo);
            
            // 启动锁续期
            startLockRenewal(lockKey, newLockInfo);
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取分布式锁（阻塞，直到获取成功或超时）
     * 
     * @param lockKey 锁的key
     * @return 是否获取成功
     * @throws InterruptedException 线程中断异常
     */
    public boolean lock(String lockKey) throws InterruptedException {
        return lock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_EXPIRE_TIME);
    }
    
    /**
     * 获取分布式锁（阻塞，直到获取成功或超时）
     * 
     * @param lockKey 锁的key
     * @param waitTime 最大等待时间（毫秒）
     * @param expireTime 锁过期时间（毫秒）
     * @return 是否获取成功
     * @throws InterruptedException 线程中断异常
     */
    public boolean lock(String lockKey, long waitTime, long expireTime) 
            throws InterruptedException {
        long endTime = System.currentTimeMillis() + waitTime;
        
        while (System.currentTimeMillis() < endTime) {
            if (tryLock(lockKey, expireTime)) {
                return true;
            }
            
            // 短暂等待后重试
            Thread.sleep(100);
        }
        
        return false;
    }
    
    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁的key
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey) {
        LockInfo lockInfo = getCurrentLockInfo(lockKey);
        if (lockInfo == null) {
            return false;
        }
        
        // 重入锁处理
        lockInfo.holdCount--;
        if (lockInfo.holdCount > 0) {
            return true;
        }
        
        // 停止锁续期
        stopLockRenewal(lockKey, lockInfo);
        
        // 使用Lua脚本原子性释放锁
        String fullLockKey = getFullLockKey(lockKey);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        Long result = stringRedisTemplate.execute(script, Collections.singletonList(fullLockKey), lockInfo.requestId);
        
        // 从ThreadLocal中移除锁信息
        currentLocks.get().remove(lockKey);
        if (currentLocks.get().isEmpty()) {
            currentLocks.remove();
        }
        
        return result != null && result > 0;
    }
    
    /**
     * 在分布式锁中执行操作（推荐使用）
     * 
     * @param lockKey 锁的key
     * @param supplier 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_EXPIRE_TIME, supplier);
    }
    
    /**
     * 在分布式锁中执行操作（推荐使用）
     * 
     * @param lockKey 锁的key
     * @param waitTime 最大等待时间（毫秒）
     * @param expireTime 锁过期时间（毫秒）
     * @param supplier 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long expireTime, Supplier<T> supplier) {
        try {
            boolean locked = lock(lockKey, waitTime, expireTime);
            if (!locked) {
                throw new RuntimeException("获取分布式锁超时，lockKey: " + lockKey);
            }
            
            try {
                return supplier.get();
            } finally {
                unlock(lockKey);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁时线程被中断", e);
        }
    }
    
    /**
     * 在分布式锁中执行无返回值的操作
     * 
     * @param lockKey 锁的key
     * @param runnable 要执行的操作
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, () -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * 强制释放锁（危险操作，慎用）
     * 
     * @param lockKey 锁的key
     * @return 是否释放成功
     */
    public boolean forceUnlock(String lockKey) {
        String fullLockKey = getFullLockKey(lockKey);
        Boolean result = stringRedisTemplate.delete(fullLockKey);
        return Boolean.TRUE.equals(result);
    }
    
    /**
     * 检查锁是否被当前线程持有
     * 
     * @param lockKey 锁的key
     * @return 是否持有锁
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        return getCurrentLockInfo(lockKey) != null;
    }
    
    /**
     * 获取锁的剩余生存时间
     * 
     * @param lockKey 锁的key
     * @return 剩余时间（毫秒），-1表示永不过期，-2表示锁不存在
     */
    public long getLockTtl(String lockKey) {
        String fullLockKey = getFullLockKey(lockKey);
        Long ttl = stringRedisTemplate.getExpire(fullLockKey, TimeUnit.MILLISECONDS);
        return ttl != null ? ttl : -2;
    }
    
    /**
     * 生成排序的锁key（用于两个用户ID的私聊场景）
     * 
 * 此方法用于生成一个排序后的锁key，确保无论传入的用户ID顺序如何，
 * 生成的key都是一致的，这样可以避免同一对话被两个不同的锁key锁定。
 *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 排序后的锁key，格式为"private_chat:较小用户ID:较大用户ID"
     */
    public String generateSortedLockKey(Long userId1, Long userId2) {
    // 使用Math.min获取两个用户ID中较小的值
        Long minUserId = Math.min(userId1, userId2);
    // 使用Math.max获取两个用户ID中较大的值
        Long maxUserId = Math.max(userId1, userId2);
    // 拼接并返回格式化的锁key
        return "private_chat:" + minUserId + ":" + maxUserId;
    }
    
    /**
     * 生成排序的锁key（用于多个ID的场景）
     * 
     * @param ids ID列表
     * @return 排序后的锁key
     */
    public String generateSortedLockKey(Long... ids) {
    // 将可变参数ids转换为ArrayList
        List<Long> idList = new ArrayList<>(Arrays.asList(ids));
    // 对ID列表进行排序，确保生成的锁key顺序一致
        Collections.sort(idList);
    // 将排序后的ID列表转换为字符串数组，并用冒号连接成最终的锁key
        return String.join(":", idList.stream().map(String::valueOf).toArray(String[]::new));
    }
    
    // 私有方法
    
/**
 * 获取完整的锁键
 * 该方法用于在原有的锁键前面添加前缀，形成完整的锁键
 * @param lockKey 原始锁键
 * @return 添加了前缀后的完整锁键
 */
    private String getFullLockKey(String lockKey) {
        // 返回LOCK_PREFIX与传入的lockKey拼接后的结果
        return LOCK_PREFIX + lockKey;
    }
    
/**
 * 获取当前线程指定锁键的锁信息
 * @param lockKey 锁的唯一标识键
 * @return 返回对应锁键的锁信息对象，如果不存在则返回null
 */
    private LockInfo getCurrentLockInfo(String lockKey) {
    // 从当前线程的局部变量中获取锁映射表，然后根据lockKey获取对应的锁信息
        return currentLocks.get().get(lockKey);
    }
    
/**
 * 启动锁续期任务，用于防止锁在业务执行过程中过期
 * @param lockKey 锁的键名
 * @param lockInfo 锁的信息对象，包含过期时间、请求ID等
 */
    private void startLockRenewal(String lockKey, LockInfo lockInfo) {
        // 如果锁设置的是永不过期，则直接返回，不需要续期
        if (lockInfo.expireTime <= 0) {
            return; // 永不过期的锁不需要续期
        }
        
        // 创建一个守护线程的定时器，用于执行锁续期任务
        lockInfo.renewalTimer = new Timer("LockRenewal-" + lockKey, true);
        // 安排定时任务，每expireTime/3毫秒执行一次续期操作
        lockInfo.renewalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 检查当前锁信息是否与传入的锁信息一致，确保锁未被其他线程更新
                if (getCurrentLockInfo(lockKey) == lockInfo) {
                    // 使用Lua脚本原子性续期，确保续期操作的原子性
                    String fullLockKey = getFullLockKey(lockKey);
                    DefaultRedisScript<Long> script = new DefaultRedisScript<>(RENEW_SCRIPT, Long.class);
                    // 执行续期脚本，传入锁的键名、请求ID和新的过期时间
                    Long result = stringRedisTemplate.execute(script,
                            Collections.singletonList(fullLockKey), 
                            lockInfo.requestId, 
                            String.valueOf(lockInfo.expireTime));
                    
                    // 如果续期失败（result为null或0），可能是锁已被释放或过期
                    if (result == null || result == 0) {
                        // 续期失败，可能是锁已被释放或过期
                        this.cancel();
                    }
                } else {
                    // 锁已被释放，停止续期
                    this.cancel();
                }
            }
        }, lockInfo.expireTime / 3, lockInfo.expireTime / 3);
    }
    
/**
 * 停止锁的续约操作
 * @param lockKey 锁的键值
 * @param lockInfo 锁的信息对象，包含续约计时器等
 */
    private void stopLockRenewal(String lockKey, LockInfo lockInfo) {
    // 检查续约计时器是否不为空
        if (lockInfo.renewalTimer != null) {
        // 取消续约计时器
            lockInfo.renewalTimer.cancel();
        // 将计时器引用置为null，帮助垃圾回收
            lockInfo.renewalTimer = null;
        }
    }
}