package org.com.timess.retrochat.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("80xia*L1N_4#SSSD*@/T6Ds{Q&R@6CMz".getBytes());
    private static final long EXPIRE_TIME = 24L * 30 * 60 * 60 * 1000;

    public static String generateToken(String username, Long userId, String  avatarUrl) {
        // 添加自定义声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        claims.put("userAvatar", avatarUrl);
        return Jwts.builder()
                .subject(username)
                .id(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .claims(claims)
                .signWith(SECRET_KEY)
                .compact();
    }



    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}