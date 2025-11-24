package org.com.timess.retrochat.model.dto.chat;

import lombok.Data;
import java.util.Map;

/**
 * WebRTC信令消息 - 纯内存传输，不映射数据库
 */
@Data
public class WebRTCSignalDTO {
    // OFFER/ANSWER/ICE_CANDIDATE
    private String type;
    // 发送者用户名
    private String from;
    // 接收者用户名
    private String to;
    // 房间ID
    private String roomId;
    // SDP/ICE候选数据
    private Map<String, Object> data;
    // 时间戳
    private Long timestamp;
    
    public static class SignalType {
        public static final String OFFER = "OFFER";
        public static final String ANSWER = "ANSWER"; 
        public static final String ICE_CANDIDATE = "ICE_CANDIDATE";
        public static final String JOIN = "JOIN";
        public static final String LEAVE = "LEAVE";
    }
}