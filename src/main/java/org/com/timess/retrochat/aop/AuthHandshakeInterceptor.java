package org.com.timess.retrochat.aop;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.com.timess.retrochat.utils.JwtUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;


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
            //从请求中获取token
//            List<String> token = request.getHeaders().get("Authorization");
//            if(CollectionUtils.isEmpty(token)){
//                log.error("登录缺少token，访问被拒绝");
//                return false;
//            }
//            Claims claims = JwtUtil.parseToken(token.getFirst());
//            attributes.put("userId", claims.get("userId"));
//            attributes.put("username", claims.get("username"));

            String token = request.getURI().getQuery().split("Authorization=")[1];
            Claims claims = JwtUtil.parseToken(token);
            attributes.put("userId", claims.get("userId"));
            attributes.put("username", claims.get("username"));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}