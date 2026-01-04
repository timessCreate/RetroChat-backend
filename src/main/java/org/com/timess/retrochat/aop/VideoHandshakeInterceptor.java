package org.com.timess.retrochat.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 视频握手拦截器 - 验证视频通话权限
 */
public class VideoHandshakeInterceptor implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, 
                                   ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, 
                                   Map<String, Object> attributes) throws Exception {
        
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            
            // 获取token和用户信息
            String token = httpServletRequest.getParameter("token");
            String userId = httpServletRequest.getParameter("userId");
            String callId = httpServletRequest.getParameter("callId");
            
            // 验证视频通话权限（这里可以添加业务逻辑）
            if (token == null || userId == null) {
                return false; // 拒绝连接
            }
            
            // 将用户信息存储在attributes中
            attributes.put("userId", userId);
            attributes.put("token", token);
            attributes.put("callId", callId);
            
            // 可以添加更多的验证逻辑，比如：
            // 1. 验证token有效性
            // 2. 检查用户是否有权限进行视频通话
            // 3. 验证callId是否有效
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, 
                               ServerHttpResponse response, 
                               WebSocketHandler wsHandler, 
                               Exception exception) {
        // 握手成功后可以执行的操作
    }
}