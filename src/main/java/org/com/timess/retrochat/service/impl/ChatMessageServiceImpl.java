package org.com.timess.retrochat.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.com.timess.retrochat.mapper.ChatMessageMapper;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.model.dto.chat.ChatMessageDTO;
import org.com.timess.retrochat.model.entity.chat.ChatRoom;
import org.com.timess.retrochat.service.ChatMessageService;
import org.com.timess.retrochat.service.ChatRoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天消息表 服务层实现。
 *
 * @author eternal
 */
@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>  implements ChatMessageService {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ChatRoomService chatRoomService;

    @Resource
    @Lazy
    private ChatMessageService self;

    @Override
    public boolean savePrivateMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = new ChatMessage();
        BeanUtils.copyProperties(chatMessageDTO, chatMessage);
        chatMessage.setTimestamp(LocalDateTime.now());
        Snowflake snowflake = IdUtil.getSnowflake();
        chatMessage.setId(snowflake.nextId());
        try {
            transactionTemplate.execute(
                    status -> {
                        try {
                            //获取聊天室
                            ChatRoom chatRoom = null;
                            if (chatMessage.getChatRoomId() != null) {
                                chatRoom = chatRoomService.getById(chatMessage.getChatRoomId());
                            }
                            //判定聊天室是否为空，如果为空，则为双方创建聊天室
                            if (chatRoom == null) {
                                chatRoom = chatRoomService.createPrivateChatRoom(chatMessage);
                                //更新消息所属的
                            }
                            //存储消息到库表
                            chatMessage.setChatRoomId(chatRoom.getId());
                            self.save(chatMessage);
                            chatRoom.setLastMessageId(chatMessage.getId());
                            chatRoom.setLastMessageContent(chatMessage.getContent());
                            chatRoom.setLastActivityTime(LocalDateTime.now());
                            chatRoomService.updateById(chatRoom);
                            return "success";
                        } catch (Exception e) {
                            status.setRollbackOnly();
                            throw e;
                        }
                    }
            );
        }catch (Exception e){
            log.error("消息保存失败：" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean savePublicMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = new ChatMessage();
        BeanUtils.copyProperties(chatMessageDTO, chatMessage);
        transactionTemplate.execute(
                status -> {
                    try{
                        //存储消息到库表
                        this.save(chatMessage);
                        //获取聊天室
                        ChatRoom chatRoom = chatRoomService.getById(chatMessage.getChatRoomId());
                        return "success";
                    }catch (Exception e){
                        status.setRollbackOnly();
                        throw e;
                    }
                }
        );
        return true;
    }

    @Override
    public boolean deleteMessage(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<ChatMessageDTO> getHistoryChatMessage(long roomId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("chat_room_id", roomId);
        List<ChatMessage> list = this.list(queryWrapper);
        return list.stream().map(chatMessage -> {
            ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
            BeanUtils.copyProperties(chatMessage, chatMessageDTO);
            return chatMessageDTO;
        }).toList();
    }
}
