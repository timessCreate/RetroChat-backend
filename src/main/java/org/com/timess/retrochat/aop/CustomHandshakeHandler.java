package org.com.timess.retrochat.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private static final String ATTR_USER = "user";

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        try {
            // 从拦截器设置的 attributes 中获取 Principal
            Object user = attributes.get(ATTR_USER);
            if (user instanceof Principal) {
                Principal principal = (Principal) user;
                log.info("HandshakeHandler 设置 Principal: {}", principal.getName());
                return principal;
            }
        } catch (Exception ex) {
            log.warn("从 attributes 获取 Principal 失败", ex);
        }

        // 回退到默认行为
        return super.determineUser(request, wsHandler, attributes);
    }
}