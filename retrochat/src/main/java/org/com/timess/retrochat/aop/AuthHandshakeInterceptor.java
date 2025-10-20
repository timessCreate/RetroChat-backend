//package org.com.timess.retrochat.aop;
//
//import cn.hutool.core.util.ObjectUtil;
//
//import jakarta.servlet.http.HttpSession;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//
//import java.util.Map;
//
///**
// * @author 33363
// * websocket认证拦截器
// * http登录的会话和websocket的会话是相互隔离的，websocket不会继承http会话的属性，
// * 本项目的websocket要共用http会话的用户属性，需要获取并存储到websocket会话中
// */
//public class AuthHandshakeInterceptor implements HandshakeInterceptor {
//
//    @Override
//    public boolean beforeHandshake(@NotNull ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        if(request instanceof  ServletServerHttpRequest){
//            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
//            HttpSession session = serverHttpRequest.getServletRequest().getSession();
//            User attribute = (User) session.getAttribute(USER_LOGIN_STATE);
//            ThrowUtils.throwIf(ObjectUtil.isEmpty(attribute), ErrorCode.NOT_LOGIN_ERROR);
//            attributes.put(USER_LOGIN_STATE, attribute);
//        }
//        return true;
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                               WebSocketHandler wsHandler, Exception exception) {}
//}