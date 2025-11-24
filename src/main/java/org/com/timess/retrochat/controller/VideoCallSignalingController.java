package org.com.timess.retrochat.controller;

import lombok.extern.slf4j.Slf4j;
import org.com.timess.retrochat.model.dto.chat.WebRTCSignalDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * @author eternal
 */
@Slf4j
@Controller
public class VideoCallSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    public VideoCallSignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 处理WebRTC Offer信令 - 不存储数据库
     */
    @MessageMapping("/webrtc/offer/{targetUser}")
    @SendToUser("/queue/webrtc-offer")
    public WebRTCSignalDTO handleOffer(@DestinationVariable String targetUser,
                                       @Payload WebRTCSignalDTO signal,
                                       Principal principal) {
        
        String fromUser = principal.getName();
        signal.setFrom(fromUser);
        
        log.debug("📨 WebRTC OFFER: {} -> {}", fromUser, targetUser);
        
        // 直接转发，不存储数据库
        return signal;
    }

    /**
     * 处理WebRTC Answer信令 - 不存储数据库  
     */
    @MessageMapping("/webrtc/answer/{targetUser}")
    @SendToUser("/queue/webrtc-answer")
    public WebRTCSignalDTO handleAnswer(@DestinationVariable String targetUser,
                                       @Payload WebRTCSignalDTO signal,
                                       Principal principal) {
        
        String fromUser = principal.getName();
        signal.setFrom(fromUser);
        
        log.debug("📨 WebRTC ANSWER: {} -> {}", fromUser, targetUser);
        
        // 直接转发，不存储数据库
        return signal;
    }

    /**
     * 处理ICE候选交换 - 不存储数据库
     */
    @MessageMapping("/webrtc/ice-candidate/{targetUser}")
    @SendToUser("/queue/webrtc-ice-candidate")  
    public WebRTCSignalDTO handleIceCandidate(@DestinationVariable String targetUser,
                                             @Payload WebRTCSignalDTO signal,
                                             Principal principal) {
        
        String fromUser = principal.getName();
        signal.setFrom(fromUser);
        
        log.debug("📨 WebRTC ICE候选: {} -> {}", fromUser, targetUser);
        
        // 直接转发，不存储数据库
        return signal;
    }

    /**
     * 用户加入视频通话房间 - 只更新成员关系，不存储信令
     */
    @MessageMapping("/webrtc/join/{roomId}")
    public void joinVideoRoom(@DestinationVariable String roomId,
                             Principal principal) {
        
        String username = principal.getName();
        
        log.info("🎥 用户 {} 加入视频房间 {}", username, roomId);
        
        // 1. 更新房间成员关系（持久化）
        // roomService.joinRoom(roomId, username);
        
        // 2. 通知其他用户（信令，不持久化）
        WebRTCSignalDTO joinSignal = new WebRTCSignalDTO();
        joinSignal.setType(WebRTCSignalDTO.SignalType.JOIN);
        joinSignal.setFrom(username);
        joinSignal.setRoomId(roomId);
        joinSignal.setTimestamp(System.currentTimeMillis());
        
        // 广播给房间内其他用户
        messagingTemplate.convertAndSend(
            "/topic/video-room/" + roomId + "/user-joined", 
            joinSignal
        );
    }

    /**
     * 用户离开视频通话房间
     */
    @MessageMapping("/webrtc/leave/{roomId}")  
    public void leaveVideoRoom(@DestinationVariable String roomId,
                             Principal principal) {
        
        String username = principal.getName();
        
        log.info("🎥 用户 {} 离开视频房间 {}", username, roomId);
        
        // 1. 更新房间成员关系（持久化）
        // roomService.leaveRoom(roomId, username);
        
        // 2. 通知其他用户（信令，不持久化）
        WebRTCSignalDTO leaveSignal = new WebRTCSignalDTO();
        leaveSignal.setType(WebRTCSignalDTO.SignalType.LEAVE);
        leaveSignal.setFrom(username);
        leaveSignal.setRoomId(roomId);
        leaveSignal.setTimestamp(System.currentTimeMillis());
        
        messagingTemplate.convertAndSend(
            "/topic/video-room/" + roomId + "/user-left", 
            leaveSignal
        );
    }
}