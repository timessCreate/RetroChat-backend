package org.com.timess.retrochat.config;

import org.com.timess.retrochat.aop.AuthHandshakeInterceptor;
import org.com.timess.retrochat.aop.CustomHandshakeHandler;
import org.springframework.context.annotation.Bean;
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
        registry.addEndpoint("/chat-ws")
                .setAllowedOriginPatterns("*")
                // 添加拦截器和握手处理器
                .addInterceptors(authHandshakeInterceptor())
                .setHandshakeHandler(customHandshakeHandler());
    }

    @Bean
    public AuthHandshakeInterceptor authHandshakeInterceptor() {
        return new AuthHandshakeInterceptor();
    }

    @Bean
    public CustomHandshakeHandler customHandshakeHandler() {
        return new CustomHandshakeHandler();
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
