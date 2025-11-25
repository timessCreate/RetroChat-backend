//package org.com.timess.retrochat.aop;
//
//import com.sun.security.auth.UserPrincipal;
//import io.jsonwebtoken.Claims;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.Strings;
//import org.com.timess.retrochat.utils.JwtUtil;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//
//import java.security.Principal;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
///**
// * @author 33363
// * websocket认证拦截器
// * http登录的会话和websocket的会话是相互隔离的，websocket不会继承http会话的属性，
// * 本项目的websocket要共用http会话的用户属性，需要获取并存储到websocket会话中
// */
//@Slf4j
//public class AuthHandshakeInterceptor implements HandshakeInterceptor {
//
//    @Override
//    public boolean beforeHandshake(@NotNull ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        if(request instanceof  ServletServerHttpRequest){
//            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
//            String requestURI = servletRequest.getRequestURI();
//            String token = "";
//            Claims claims;
//
//            // 修复URL判断逻辑
//            if(requestURI.equals("/retrochat/chat-ws")){  // 去掉Strings.CS
//                // 从查询参数获取token
//                String query = request.getURI().getQuery();
//                if (StringUtils.isNotBlank(query)) {
//                    String[] params = query.split("&");
//                    for (String param : params) {
//                        if (param.startsWith("Authorization=")) {
//                            token = param.substring("Authorization=".length());
//                            break;
//                        }
//                    }
//                }
//
//                if(StringUtils.isBlank(token)){
//                    log.error("websocket握手uri缺少token，访问被拒绝");
//                    return false;
//                }
//                claims = JwtUtil.parseToken(token);
//            } else {
//                // 其他路径从header获取
//                List<String> authorization = request.getHeaders().get("Authorization");
//                if(CollectionUtils.isEmpty(authorization)){
//                    log.error("websocket发送消息缺少token，访问被拒绝");
//                    return false;
//                }
//                token = authorization.getFirst().replace("Bearer ", "");
//                claims = JwtUtil.parseToken(token);
//            }
//
//            if (claims == null) {
//                log.error("token解析失败");
//                return false;
//            }
//
//            // 设置用户信息
//            String username = (String) claims.get("username");
//            Long userId = Long.parseLong(String.valueOf(claims.get("userId")));
//
//            StompPrincipal stompPrincipal = new StompPrincipal(userId, username);
//
//            // 设置到attributes中，Spring会自动识别"user" key
//            attributes.put("user", stompPrincipal);
//            attributes.put("userId", userId);
//            attributes.put("username", username);
//
//            log.info("WebSocket认证成功: userId={}, username={}", userId, username);
//        }
//        return true;
//    }
//
//    // 添加StompPrincipal类
//    public static class StompPrincipal implements Principal {
//        private final String name;
//        private final Long userId;
//        private final String username;
//
//        public StompPrincipal(Long userId, String username) {
//            this.name = userId.toString();
//            this.userId = userId;
//            this.username = username;
//        }
//
//        @Override
//        public String getName() {
//            return name;
//        }
//
//        public Long getUserId() {
//            return userId;
//        }
//
//        public String getUsername() {
//            return username;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) return true;
//            if (obj == null || getClass() != obj.getClass()) return false;
//            StompPrincipal that = (StompPrincipal) obj;
//            return Objects.equals(name, that.name) && Objects.equals(userId, that.userId);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(name, userId);
//        }
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                               WebSocketHandler wsHandler, Exception exception) {}
//}


package org.com.timess.retrochat.aop;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Objects;

/**
 * WebSocket 握手拦截器
 *
 * 目的：
 *  - 从握手请求（query 或 header）中提取 token
 *  - 解析 token，构造 Principal（此处使用 userId 作为 name）
 *  - 将 Principal 放入 attributes 中（key="user"），配合 CustomHandshakeHandler 将 Principal 绑定到会话
 *
 * 注意：
 *  - 必须在 WebSocket 配置里使用 CustomHandshakeHandler（见注释示例）
 */
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final String ATTR_USER = "user";
    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_USERNAME = "username";

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request,
                                   @NotNull ServerHttpResponse response,
                                   @NotNull WebSocketHandler wsHandler,
                                   @NotNull Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) {
            // 非 Servlet 请求，允许通过（或按需拒绝）
            return true;
        }

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

        String requestURI = httpServletRequest.getRequestURI();
        String token = null;
        Claims claims = null;

        try {
            // 如果是通过 sockjs 或 websocket endpoint 的握手（示例路径 /retrochat/chat-ws）
            if (StringUtils.equals(requestURI, "/retrochat/chat-ws") || requestURI.endsWith("/chat-ws")) {
                // 优先从 query string 提取 token（常见客户端在 URL 上带 token）
                token = extractTokenFromQuery(request.getURI().getQuery());
                // 如果 query 中没有，再尝试 header
                if (StringUtils.isBlank(token)) {
                    List<String> authHeaders = request.getHeaders().get("Authorization");
                    if (!CollectionUtils.isEmpty(authHeaders)) {
                        token = stripBearer(authHeaders.get(0));
                    }
                }
            } else {
                // 其它路径（例如 socket-send/message）从 header 获取 Authorization
                List<String> authHeaders = request.getHeaders().get("Authorization");
                if (!CollectionUtils.isEmpty(authHeaders)) {
                    token = stripBearer(authHeaders.get(0));
                }
            }

            if (StringUtils.isBlank(token)) {
                log.error("WebSocket 握手缺少 token，访问被拒绝. uri={}, query={}", requestURI, request.getURI().getQuery());
                return false;
            }

            claims = JwtUtil.parseToken(token);
            if (claims == null) {
                log.error("token 解析失败或已过期");
                return false;
            }

            // 从 claims 中获取用户信息（字段名按照你的 JWT 载荷而定）
            Object userIdObj = claims.get("userId");
            Object usernameObj = claims.get("username");
            if (userIdObj == null) {
                log.error("token 中缺少 userId");
                return false;
            }

            Long userId;
            try {
                userId = Long.parseLong(String.valueOf(userIdObj));
            } catch (NumberFormatException ex) {
                log.error("userId 不是合法数字: {}", userIdObj);
                return false;
            }

            String username = usernameObj == null ? String.valueOf(userId) : String.valueOf(usernameObj);

            StompPrincipal principal = new StompPrincipal(userId, username);

            // 将 Principal 放入 attributes 中，CustomHandshakeHandler 会读取并绑定到会话
            attributes.put(ATTR_USER, principal);
            attributes.put(ATTR_USER_ID, userId);
            attributes.put(ATTR_USERNAME, username);

            log.info("WebSocket 握手认证成功: userId={}, username={}, uri={}", userId, username, requestURI);
            return true;
        } catch (Exception ex) {
            log.error("WebSocket 握手处理异常", ex);
            return false;
        }
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request,
                               @NotNull ServerHttpResponse response,
                               @NotNull WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private static String extractTokenFromQuery(String query) {
        if (StringUtils.isBlank(query)) return null;
        // 支持多个常见参数名：Authorization=..., token=..., access_token=...
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("Authorization=")) {
                return param.substring("Authorization=".length());
            }
            if (param.startsWith("token=")) {
                return param.substring("token=".length());
            }
            if (param.startsWith("access_token=")) {
                return param.substring("access_token=".length());
            }
        }
        return null;
    }

    private static String stripBearer(String header) {
        if (StringUtils.isBlank(header)) return null;
        if (header.toLowerCase().startsWith("bearer ")) {
            return header.substring(7).trim();
        }
        return header.trim();
    }

    /**
     * 简单的 Principal 实现，name 使用 userId 的字符串形式（这能方便通过 convertAndSendToUser 使用 userId 作为目标）
     */
    public static class StompPrincipal implements Principal {
        private final String name;
        private final Long userId;
        private final String username;

        public StompPrincipal(Long userId, String username) {
            this.name = String.valueOf(userId);
            this.userId = userId;
            this.username = username;
        }

        @Override
        public String getName() {
            return name;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            StompPrincipal that = (StompPrincipal) obj;
            return Objects.equals(name, that.name) && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, userId);
        }
    }
}