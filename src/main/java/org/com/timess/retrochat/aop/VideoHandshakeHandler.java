package org.com.timess.retrochat.aop;

import lombok.Getter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 视频握手处理器 - 设置视频通话的Principal
 */
public class VideoHandshakeHandler extends DefaultHandshakeHandler {
    
    @Override
    protected Principal determineUser(ServerHttpRequest request, 
                                      WebSocketHandler wsHandler, 
                                      Map<String, Object> attributes) {
        // 从attributes中获取用户信息
        String userId = (String) attributes.get("userId");
        String callId = (String) attributes.get("callId");
        
        if (userId != null) {
            // 创建自定义Principal，包含视频通话相关信息
            return new VideoPrincipal(userId, callId);
        }
        return null;
    }
    
    // 自定义视频通话Principal
    @Getter
    public static class VideoPrincipal implements Principal {
        private final String userId;
        private final String callId;
        
        public VideoPrincipal(String userId, String callId) {
            this.userId = userId;
            this.callId = callId;
        }
        
        @Override
        public String getName() {
            return userId;
        }

    }
}