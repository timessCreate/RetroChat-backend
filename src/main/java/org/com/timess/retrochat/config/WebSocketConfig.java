package org.com.timess.retrochat.config;

import org.com.timess.retrochat.aop.AuthHandshakeInterceptor;
import org.com.timess.retrochat.aop.CustomHandshakeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * @author 33363
 * webSocket核心配置类
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 暴露 WebSocket 端点，支持 SockJS 回退
        registry.addEndpoint("/chat-ws")
                // 指定自定义的 HandshakeHandler，把 attributes 中的 "user" 绑定为会话 Principal
                .setHandshakeHandler(new CustomHandshakeHandler())
                //允许跨域
                .setAllowedOriginPatterns("*")
                //添加认证拦截器
                .addInterceptors(new AuthHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单内存消息代理（生产环境建议用 RabbitMQ 或 Kafka） 对应@SendTo路径
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息的前缀 对应 @MessageMapping 路径
        registry.setApplicationDestinationPrefixes("/app");
        // 点对点消息前缀（默认 /user） -->
        registry.setUserDestinationPrefix("/user");
    }
    
    // 配置消息缓冲区大小
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 128KB
        registry.setMessageSizeLimit(128 * 1024);
        // 512KB
        registry.setSendBufferSizeLimit(512 * 1024);
    }
}
