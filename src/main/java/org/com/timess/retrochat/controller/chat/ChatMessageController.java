package org.com.timess.retrochat.controller.chat;

import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.com.timess.retrochat.common.BaseResponse;
import org.com.timess.retrochat.common.ResultUtils;
import org.com.timess.retrochat.exception.BusinessException;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.model.dto.chat.ChatMessageDTO;
import org.com.timess.retrochat.model.dto.chat.ChatPageRequest;
import org.com.timess.retrochat.model.entity.chat.ChatMessage;
import org.com.timess.retrochat.service.ChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息表 控制层。
 * @author eternal
 */
@Slf4j
@RestController
public class ChatMessageController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private  ChatMessageService chatMessageService;

    /**
     * 发送文本消息 - 需要存储到数据库
     */
    @PostMapping("/chat/")
    public BaseResponse<String> sendTextMessage(@DestinationVariable String roomId,
                                @Payload ChatMessageDTO messageDTO,
                                SimpMessageHeaderAccessor headerAccessor) {
        // 从消息头中获取用户信息，避免Principal为null的问题
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : 
                         (String) headerAccessor.getSessionAttributes().get("username");
        
        if (username == null) {
            log.error("无法获取用户信息，拒绝发送消息");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"无法获取用户信息，拒绝发送消息");
        }
        try {
            // 1. 保存到数据库（持久化）
            chatMessageService.savePublicMessage(messageDTO);
            log.info("💬 用户 {} 在房间 {} 发送消息: {}", username, roomId, messageDTO.getContent());
            
            // 2. 构建消息对象
            Map<String, Object> message = new HashMap<>();
            message.put("sender", username);
            message.put("content", messageDTO.getContent());
            message.put("timestamp", messageDTO.getTimestamp());
            
            // 3. 广播给房间内所有用户
            messagingTemplate.convertAndSend(
                    "/topic/chat-room/" + roomId,
                    message
            );
            //TODO: 添加消息确认机制
            return ResultUtils.success("success");
        } catch (Exception e) {
            log.error("保存聊天消息失败", e);
            // 发送错误消息给发送者
            messagingTemplate.convertAndSendToUser(
                    username, "/queue/errors",
                    Map.of("error", "发送消息失败")
            );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"发送消息失败");
        }
    }
    /**
     * 发送私聊消息
     */
    @PostMapping("/chat/private")
    public BaseResponse<ChatMessageDTO> sendPrivateMessage(@RequestBody ChatMessageDTO messageDTO, HttpServletRequest request) {
        // 从消息头中获取用户信息，避免Principal为null的问题
        String senderName = (String) request.getAttribute("username");
        Long senderId = (Long) request.getAttribute("userId");

        if (senderName == null) {
            log.error("无法获取用户信息，拒绝发送私聊消息");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"无法获取用户信息，拒绝发送私聊消息");
        }
        // 接收者用户名
        String receiverId = String.valueOf(messageDTO.getReceiverId());
        try {
            log.info("🔒 用户 {} 向 {} 发送私聊消息: {}", senderName, receiverId, messageDTO.getContent());
            
            // 设置发送者信息
            messageDTO.setSenderId(senderId);
            messageDTO.setSenderName(senderName);
            messageDTO.setMessageType(2);
            // 1. 保存消息内容
            ChatMessage chatMessage = chatMessageService.savePrivateMessage(messageDTO);

            // 3. 发送给接收者
            messagingTemplate.convertAndSendToUser(
                    receiverId,
                    "/queue/private",
                    chatMessage.getDTO()
            );
            //TODO: 添加消息确认机制
            return ResultUtils.success(chatMessage.getDTO());
        } catch (Exception e) {
            log.error("发送私聊消息失败", e);
            messagingTemplate.convertAndSendToUser(
                    senderName, "/queue/errors",
                    Map.of("error", "发送私聊消息失败")
            );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"发送私聊消息失败");
        }
    }

    @GetMapping("/chat/history")
    public BaseResponse<List<ChatMessageDTO>> getHistoryChatMessage(@RequestParam String roomId) {
        try {
            return ResultUtils.success(chatMessageService.getHistoryChatMessage(Long.parseLong(roomId)));
        }catch (Exception e){
            log.error("获取历史消息失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取历史消息失败");
        }
    }

    @PostMapping("/chat/page-history")
    public BaseResponse<Page<ChatMessageDTO>> getHistoryPageChatMessage(@RequestBody ChatPageRequest request) {
        try {
            return ResultUtils.success(chatMessageService.getHistoryPageChatMessage(request));
        }catch (Exception e){
            log.error("获取历史消息失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取历史消息失败");
        }
    }
}
