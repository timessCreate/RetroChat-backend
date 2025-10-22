package org.com.timess.retrochat.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Token黑名单服务，结合布隆过滤器和Redis实现高效的Token黑名单管理
 */
@Service
public class TokenBlacklistService {

    @Resource
    private final StringRedisTemplate stringRedisTemplate;

    private final BloomFilter<String> bloomFilter;

    // Redis键前缀
    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    // 布隆过滤器配置：预计100万token，假阳性率0.1%
    private static final long EXPECTED_INSERTIONS = 1_000_000L;
    private static final double FALSE_POSITIVE_RATE = 0.001;

    @Autowired
    public TokenBlacklistService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(Charset.defaultCharset()),
                EXPECTED_INSERTIONS,
                FALSE_POSITIVE_RATE
        );

        // 可选：应用启动时预热布隆过滤器（加载已有的黑名单token）
        warmUpBloomFilter();
    }

    /**
     * 检查token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // 第一步：布隆过滤器快速排除（如果返回false，100%不在黑名单）
        if (!bloomFilter.mightContain(token)) {
            return false;
        }

        // 第二步：Redis精确验证
        String redisKey = BLACKLIST_KEY_PREFIX + token;
        return stringRedisTemplate.hasKey(redisKey);
    }

    /**
     * 添加token到黑名单
     * @param token token值
     * @param ttlMillis 过期时间（毫秒）
     */
    public void addToBlacklist(String token, long ttlMillis) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("TTL must be greater than 0");
        }

        String redisKey = BLACKLIST_KEY_PREFIX + token;

        // 添加到Redis（设置过期时间）
        stringRedisTemplate.opsForValue().set(redisKey, "1", ttlMillis, TimeUnit.MILLISECONDS);

        // 添加到布隆过滤器
        bloomFilter.put(token);
    }

    /**
     * 添加token到黑名单（基于token本身的过期时间）
     * @param token token值
     * @param tokenExpiresAt token过期时间戳（毫秒）
     */
    public void addToBlacklistWithTokenExpiry(String token, long tokenExpiresAt) {
        long currentTime = System.currentTimeMillis();
        if (tokenExpiresAt <= currentTime) {
            // token已过期，不需要加入黑名单
            return;
        }

        // 黑名单有效期 = token过期时间 + 缓冲时间（5分钟）
        long ttlMillis = tokenExpiresAt - currentTime + (5 * 60 * 1000);
        addToBlacklist(token, ttlMillis);
    }

    /**
     * 添加token到黑名单（默认48小时过期）
     */
    public void addToBlacklist(String token) {
        // 默认永久不允许使用，设置为48小时后过期以节省空间
        addToBlacklist(token, 48L * 60 * 60 * 1000L);
    }

    /**
     * 手动从黑名单移除token（注意：布隆过滤器不支持删除）
     * 主要用于测试或特殊情况
     */
    public boolean removeFromBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String redisKey = BLACKLIST_KEY_PREFIX + token;
        Boolean deleted = stringRedisTemplate.delete(redisKey);
        // 注意：布隆过滤器中的记录无法删除，但Redis删除后就不会误判了
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * 获取黑名单中token的剩余存活时间
     */
    public Long getTokenTtl(String token) {
        if (token == null || token.trim().isEmpty()) {
            return -2L; // key不存在
        }
        String redisKey = BLACKLIST_KEY_PREFIX + token;
        return stringRedisTemplate.getExpire(redisKey, TimeUnit.MILLISECONDS);
    }

    /**
     * 批量检查多个token（优化性能）
     */
    public Map<String, Boolean> batchCheckBlacklist(List<String> tokens) {
        Map<String, Boolean> results = new HashMap<>();

        // 先使用布隆过滤器快速筛选
        List<String> needRedisCheck = tokens.stream()
                .filter(token -> token != null && !token.trim().isEmpty())
                .filter(token -> bloomFilter.mightContain(token))
                .collect(Collectors.toList());

        // 使用管道批量查询Redis
        if (!needRedisCheck.isEmpty()) {
            // 明确指定使用RedisCallback
            List<Object> pipelineResults = stringRedisTemplate.executePipelined(
                    (RedisCallback<Object>) connection -> {
                        for (String token : needRedisCheck) {
                            String redisKey = BLACKLIST_KEY_PREFIX + token;
                            connection.exists(redisKey.getBytes());
                        }
                        return null; // RedisCallback需要返回值，但实际不需要
                    }
            );

            // 处理结果
            for (int i = 0; i < needRedisCheck.size(); i++) {
                String token = needRedisCheck.get(i);
                Boolean exists = (Boolean) pipelineResults.get(i);
                results.put(token, Boolean.TRUE.equals(exists));
            }
        }

        // 填充未通过布隆过滤器的结果
        tokens.stream()
                .filter(token -> token != null && !token.trim().isEmpty())
                .filter(token -> !results.containsKey(token))
                .forEach(token -> results.put(token, false));

        return results;
    }

    /**
     * 获取当前黑名单中的token数量（近似值）
     */
    public long getBlacklistSize() {
        // 注意：这只是近似值，因为Redis中的key可能已经过期但还未被删除
        return bloomFilter.approximateElementCount();
    }

    /**
     * 清理所有黑名单数据（危险操作，仅用于测试）
     */
    public void clearAllBlacklist() {
        // 删除所有黑名单相关的Redis键
        Set<String> keys = stringRedisTemplate.keys(BLACKLIST_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }

        // 注意：布隆过滤器无法清空，需要重新创建
        // 在实际生产环境中，不建议使用此方法
    }

    /**
     * 检查服务健康状态
     */
    public HealthCheckResult healthCheck() {
        try {
            // 测试Redis连接
            stringRedisTemplate.hasKey(BLACKLIST_KEY_PREFIX + "healthcheck");

            // 测试布隆过滤器
            bloomFilter.mightContain("healthcheck");

            return new HealthCheckResult(true, "Service is healthy");
        } catch (Exception e) {
            return new HealthCheckResult(false, "Service unhealthy: " + e.getMessage());
        }
    }

    /**
     * 预热布隆过滤器（加载已有的黑名单token）
     */
    private void warmUpBloomFilter() {
        // 可选：在应用启动时，从Redis加载已有的黑名单token到布隆过滤器
        // 注意：这可能会消耗较多资源，根据实际情况决定是否启用
        try {
            Set<String> keys = stringRedisTemplate.keys(BLACKLIST_KEY_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    String token = key.substring(BLACKLIST_KEY_PREFIX.length());
                    bloomFilter.put(token);
                }
                System.out.println("布隆过滤器预热完成，加载了 " + keys.size() + " 个token");
            }
        } catch (Exception e) {
            System.err.println("布隆过滤器预热失败: " + e.getMessage());
        }
    }

    /**
     * 获取布隆过滤器统计信息（用于监控）
     */
    public BloomFilterStats getBloomFilterStats() {
        return new BloomFilterStats(
                bloomFilter.approximateElementCount(),
                bloomFilter.expectedFpp(),
                FALSE_POSITIVE_RATE
        );
    }

    // 统计信息类
    public static class BloomFilterStats {
        private final long approximateElementCount;
        private final double currentFalsePositiveProbability;
        private final double configuredFalsePositiveRate;

        public BloomFilterStats(long approximateElementCount,
                                double currentFalsePositiveProbability,
                                double configuredFalsePositiveRate) {
            this.approximateElementCount = approximateElementCount;
            this.currentFalsePositiveProbability = currentFalsePositiveProbability;
            this.configuredFalsePositiveRate = configuredFalsePositiveRate;
        }

        public long getApproximateElementCount() {
            return approximateElementCount;
        }

        public double getCurrentFalsePositiveProbability() {
            return currentFalsePositiveProbability;
        }

        public double getConfiguredFalsePositiveRate() {
            return configuredFalsePositiveRate;
        }

        @Override
        public String toString() {
            return String.format("BloomFilterStats{elementCount=%d, currentFpp=%.4f, configuredFpp=%.4f}",
                    approximateElementCount, currentFalsePositiveProbability, configuredFalsePositiveRate);
        }
    }

    // 健康检查结果类
    public static class HealthCheckResult {
        private final boolean healthy;
        private final String message;

        public HealthCheckResult(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("HealthCheckResult{healthy=%s, message='%s'}", healthy, message);
        }
    }
}