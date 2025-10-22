package org.com.timess.retrochat.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("80xia*L1N_4#SSSD*@/T6Ds{Q&R@6CMz".getBytes());
    private static final long EXPIRE_TIME = 24L * 30 * 60 * 60 * 1000;

    public static String generateToken(String username, Long userId) {
        return Jwts.builder()
                .subject(username)
                .id(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
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