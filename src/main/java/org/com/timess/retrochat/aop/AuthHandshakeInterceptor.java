package org.com.timess.retrochat.aop;

import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.com.timess.retrochat.utils.JwtUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;


import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * @author 33363
 * websocket认证拦截器
 * http登录的会话和websocket的会话是相互隔离的，websocket不会继承http会话的属性，
 * 本项目的websocket要共用http会话的用户属性，需要获取并存储到websocket会话中
 */
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if(request instanceof  ServletServerHttpRequest){
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String requestURI = servletRequest.getRequestURI();
            String token = "";
            Claims claims;
            if(Strings.CS.equals(requestURI,"/retrochat/chat-ws")){
                //从请求中获取token
                token = request.getURI().getQuery().split("Authorization=")[1];
                if(StringUtils.isBlank(token)){
                    log.error("websocket握手uri缺少token，访问被拒绝");
                    return false;
                }
                claims = JwtUtil.parseToken(token);
            }else{
                List<String> authorization = request.getHeaders().get("Authorization");
                if(CollectionUtils.isEmpty(authorization)){
                    log.error("websocket发送消息缺少token，访问被拒绝");
                    return false;
                }
                claims = JwtUtil.parseToken(authorization.getFirst());
            }
            // 关键修改：确保Principal正确设置
            String username = (String) claims.get("username");
            UserPrincipal userPrincipal = new UserPrincipal(username);
            StompPrincipal stompPrincipal = new StompPrincipal(username);
            
            // 设置用户信息到session属性
            attributes.put("simpUser", stompPrincipal);
            attributes.put("SPRING.SESSION.USER_PRINCIPAL", userPrincipal);
            attributes.put("userId", Long.parseLong(String.valueOf(claims.get("userId"))));
            attributes.put("username", username);
        }
        return true;
    }

    // 添加StompPrincipal类
    private static class StompPrincipal implements Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}