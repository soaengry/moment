package com.soaengry.moment.global.security;

import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    public void init() {
        String secret = jwtProperties.secret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret은 최소 32자 이상이어야 합니다");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("token_version", user.getTokenVersion());
        claims.put("nonce", UUID.randomUUID().toString());

        return generateToken(claims, user.getId().toString(), jwtProperties.accessTokenExpiration());
    }

    public String generateRefreshToken(Long userId, String deviceId, Integer tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("device_id", deviceId);
        claims.put("token_version", tokenVersion);
        claims.put("nonce", UUID.randomUUID().toString());

        return generateToken(claims, userId.toString(), jwtProperties.refreshTokenExpiration());
    }

    private String generateToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public Integer getTokenVersionFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("token_version", Integer.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    public String getDeviceIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("device_id", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
