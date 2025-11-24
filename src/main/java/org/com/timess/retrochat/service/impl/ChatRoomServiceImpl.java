package org.com.timess.retrochat.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.com.timess.retrochat.enums.ChatRoomEnums;
import org.com.timess.retrochat.exception.BusinessException;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.mapper.ChatRoomMapper;
import org.com.timess.retrochat.model.dto.chat.ChatMessageDTO;
import org.com.timess.retrochat.model.dto.chat.ChatRoomDTO;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.com.timess.retrochat.model.vo.UserVO;
import org.com.timess.retrochat.service.ChatRoomMemberService;
import org.com.timess.retrochat.service.ChatRoomService;
import org.com.timess.retrochat.service.UserService;
import org.com.timess.retrochat.utils.RedisDistributedLockUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 聊天室表 服务层实现。
 *
 * @author eternal
 */
@Service
@Slf4j
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom>  implements ChatRoomService{

    @Resource
    UserService userService;

    // 注入自身代理
    @Resource
    @Lazy
    private ChatRoomService self;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    RedisDistributedLockUtil redisLockUtil;

    @Resource
    ChatRoomMapper chatRoomMapper;

    @Resource
    ChatRoomMemberService chatRoomMemberService;

    @Override
    public boolean saveData(ChatRoomDTO chatRoomDTO, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        ChatRoom chatRoom = new ChatRoom();
        BeanUtils.copyProperties(chatRoomDTO,chatRoom);
        chatRoom.setIsActive(1);
        chatRoom.setOwnerId(loginUser.getId());
        chatRoom.setLastActivityTime(LocalDateTime.now());
        return this.save(chatRoom);
    }


    @Override
    public ChatRoom createPrivateChatRoom(ChatMessage chatMessage) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName("chatRoom_" + chatMessage.getSenderId() + "_" + chatMessage.getReceiverId());
        chatRoom.setLastMessageId(chatMessage.getId());
        chatRoom.setType(ChatRoomEnums.ONE_TO_ONE.getType());
        chatRoom.setMaxMembers(2);
        chatRoom.setCurrentMembers(2);
        chatRoom.setIsActive(1);
        chatRoom.setOwnerId(chatMessage.getSenderId());
        chatRoom.setLastMessageId(chatMessage.getId());
        chatRoom.setLastMessageContent(chatMessage.getContent());
        chatRoom.setLastActivityTime(LocalDateTime.now());
        // 获取用户ID
        Long userId1 = chatMessage.getSenderId();
        Long userId2 = chatMessage.getReceiverId();
        String lockKey = redisLockUtil.generateSortedLockKey(userId1, userId2);
        return redisLockUtil.executeWithLock(lockKey, 5000, 30000, () -> {
            ChatRoom existingRoom = chatRoomMapper.findPrivateRoomByUserPair(userId1, userId2);
            if (existingRoom != null) {
                return existingRoom;
            }
            //插入聊天室和聊天室成员表
            try {
                transactionTemplate.execute(
                        status -> {
                            try {
                                self.save(chatRoom);
                                chatRoomMemberService.savePrivateRoomMember(chatMessage, chatRoom);
                                return "success";
                            } catch (Exception e) {
                                status.setRollbackOnly();
                                throw e;
                            }
                        }
                );
            }catch (Exception e){
                log.error("消息保存失败：" + e.getMessage());
            }
            return chatRoom;
        });
    }

    @Override
    public boolean updateData(ChatRoomDTO chatRoomDTO) {
        ChatRoom chatRoom = new ChatRoom();
        BeanUtils.copyProperties(chatRoomDTO,chatRoom);
        return this.updateById(chatRoom);
    }

    /**
     * 解散聊天室
     * @param id
     * @param request
     * @return
     */
    @Override
    public boolean delete(String id, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        ChatRoom chamRoom = this.getById(id);
        if(loginUser.getId() != (long)chamRoom.getOwnerId()){
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "仅群主可解散聊天室");
        }
        return this.removeById(id);
    }
}
