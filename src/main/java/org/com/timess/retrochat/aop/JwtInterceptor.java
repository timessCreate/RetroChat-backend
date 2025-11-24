package org.com.timess.retrochat.aop;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.com.timess.retrochat.exception.BusinessException;
import org.com.timess.retrochat.exception.ErrorCode;
import org.com.timess.retrochat.service.TokenBlacklistService;
import org.com.timess.retrochat.service.UserService;
import org.com.timess.retrochat.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author eternal
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Resource
    private UserService userService;

    @Resource
    private TokenBlacklistService tokenBlacklistService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "缺少token，访问被拒绝");
        }
        // 验证token是否在黑名单内
        if(tokenBlacklistService.isTokenBlacklisted(token)){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "token在黑名单内，该token已失效");
        }
        try {
            // 2. 解析token
            Claims claims = JwtUtil.parseToken(token);
            String userId = claims.getId();
            String username = claims.getSubject();
            
            // 3. 验证用户是否存在
            if (!userService.existsUser(Long.parseLong(userId), username)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "token无效或已过期");
            }
            // 4. 将用户信息存入request
            request.setAttribute("userId", Long.parseLong(userId));
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "token无效或已过期");
        }
    }
}