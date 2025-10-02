package com.cocomoo.taily.security.jwt;

import com.cocomoo.taily.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    // application.properties: spring.jwt.secret=비밀키문자열
    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        // HS256용 SecretKey 생성
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT SecretKey 초기화 완료");
    }

    /**
     * JWT 토큰 생성
     */
    public String createJwt(User user, long expiredMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiredMs);

        return Jwts.builder()
                .claim("id", user.getId())
                .claim("publicId", user.getPublicId())
                .claim("username", user.getUsername())
                .claim("nickname", user.getNickname())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    public String getPublicId(String token) {
        return getClaims(token).get("publicId", String.class);
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String getNickname(String token) {
        return getClaims(token).get("nickname", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("토큰 만료 확인 실패: {}", e.getMessage());
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token); // 서명 및 만료 검증 포함
            return !isExpired(token);
        } catch (JwtException e) {
            log.warn("유효하지 않은 JWT: {}", e.getMessage());
            return false;
        }
    }
}
