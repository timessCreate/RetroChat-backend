package org.com.timess.retrochat.controller.websocket;

import org.com.timess.retrochat.model.vo.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * @author eternal
 * @description: WebSocket 消息处理控制器
 */
// 注意这里仍用 @Controller，但不是处理 HTTP 请求
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 处理客户端发送到 /app/chat 的消息
    @MessageMapping("/chat") 
    @SendTo("/topic/messages") // 广播到所有订阅 /topic/messages 的客户端
    public ChatMessage handleMessage(@Payload ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    // 发送私信
    @MessageMapping("/private.{userId}")
    public void sendPrivateMessage(
            @DestinationVariable String userId,
            @Payload ChatMessage message) {
        messagingTemplate.convertAndSendToUser(
            userId, 
            "/queue/private", 
            message);
    }
}