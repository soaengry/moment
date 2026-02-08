package com.soaengry.moment.user.repository;

import com.soaengry.moment.user.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis Key 형식: refresh_token:{userId}:{deviceId}
     */
    private String generateKey(Long userId, String deviceId) {
        return KEY_PREFIX + userId + ":" + deviceId;
    }

    /**
     * RefreshToken 저장
     */
    public void save(RefreshToken token) {
        String key = generateKey(token.getUserId(), token.getDeviceId());
        long ttl = Duration.between(token.getCreatedAt(), token.getExpiresAt()).getSeconds();
        redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.SECONDS);
    }

    /**
     * Token으로 RefreshToken 조회
     */
    public Optional<RefreshToken> findByToken(String token) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        for (String key : keys) {
            RefreshToken refreshToken = (RefreshToken) redisTemplate.opsForValue().get(key);
            if (refreshToken.getToken().equals(token)) {
                return Optional.of(refreshToken);
            }
        }
        return Optional.empty();
    }

    /**
     * userId와 deviceId로 RefreshToken 조회
     */
    public Optional<RefreshToken> findByUserIdAndDeviceId(Long userId, String deviceId) {
        String key = generateKey(userId, deviceId);
        RefreshToken token = (RefreshToken) redisTemplate.opsForValue().get(key);
        return Optional.of(token);
    }

    /**
     * 특정 사용자의 모든 활성 토큰 조회
     */
    public List<RefreshToken> findActiveTokensByUserId(Long userId) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + userId + ":*");

        return keys.stream()
                .map(key -> (RefreshToken) redisTemplate.opsForValue().get(key))
                .filter(RefreshToken::isValid)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 활성 토큰 개수
     */
    public long countActiveTokensByUserId(Long userId) {
        return findActiveTokensByUserId(userId).size();
    }

    /**
     * 특정 사용자의 모든 토큰 삭제 (로그아웃 all)
     */
    public void deleteAllByUserId(Long userId) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + userId + ":*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 특정 디바이스의 토큰 삭제
     */
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        String key = generateKey(userId, deviceId);
        redisTemplate.delete(key);
    }

    /**
     * 특정 토큰 삭제
     */
    public void deleteByToken(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        refreshToken.ifPresent(rt ->
                deleteByUserIdAndDeviceId(rt.getUserId(), rt.getDeviceId())
        );
    }

    /**
     * 특정 사용자의 가장 오래된 토큰들 조회 (디바이스 제한용)
     */
    public List<RefreshToken> findOldestActiveTokensByUserId(Long userId) {
        return findActiveTokensByUserId(userId).stream()
                .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
