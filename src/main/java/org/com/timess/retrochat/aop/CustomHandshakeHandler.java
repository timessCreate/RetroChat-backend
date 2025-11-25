package org.com.timess.retrochat.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * CustomHandshakeHandler 会在握手阶段把 attributes 中的 "user"（如果存在且为 Principal）作为会话的 Principal 返回，
 * 从而让 Spring 将该 Principal 绑定到 WebSocketSession 上。
 */
@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private static final String ATTR_USER = "user";

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        try {
            Object user = attributes.get(ATTR_USER);
            if (user instanceof Principal) {
                return (Principal) user;
            }
        } catch (Exception ex) {
            log.warn("从 attributes 获取 Principal 失败", ex);
        }
        // 回退到默认行为（可能是 null 或容器提供的默认 principal）
        return super.determineUser(request, wsHandler, attributes);
    }
}