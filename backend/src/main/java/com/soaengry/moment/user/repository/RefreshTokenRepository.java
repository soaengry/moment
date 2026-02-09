package com.soaengry.moment.user.repository;

import com.soaengry.moment.global.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";
    private static final long TTL_DAYS = 7;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis Key 형식: refresh_token:{userId}:{deviceId}
     */
    private String generateKey(Long userId, String deviceId) {
        return KEY_PREFIX + userId + ":" + deviceId;
    }

    /**
     * RefreshToken 저장 (해시값만 저장)
     */
    public void save(Long userId, String deviceId, String token) {
        String key = generateKey(userId, deviceId);
        String hashedToken = TokenHashUtil.hashToken(token);
        stringRedisTemplate.opsForValue().set(key, hashedToken, TTL_DAYS, TimeUnit.DAYS);
        log.debug("RefreshToken 저장 완료 - Key: {}", key);
    }

    /**
     * Token이 유효한지 검증
     */
    public boolean existsByToken(Long userId, String deviceId, String token) {
        String key = generateKey(userId, deviceId);
        String storedHash = stringRedisTemplate.opsForValue().get(key);
        if (storedHash == null) {
            return false;
        }
        String tokenHash = TokenHashUtil.hashToken(token);
        return storedHash.equals(tokenHash);
    }

    /**
     * 특정 사용자의 활성 토큰 개수
     */
    public long countActiveTokensByUserId(Long userId) {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + userId + ":*");
        return keys.size();
    }

    /**
     * 특정 사용자의 모든 토큰 삭제 (로그아웃 all)
     */
    public void deleteAllByUserId(Long userId) {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + userId + ":*");
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.debug("모든 RefreshToken 삭제 완료 - 사용자 ID: {}, 개수: {}", userId, keys.size());
        }
    }

    /**
     * 특정 디바이스의 토큰 삭제
     */
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        String key = generateKey(userId, deviceId);
        stringRedisTemplate.delete(key);
        log.debug("RefreshToken 삭제 완료 - Key: {}", key);
    }

    /**
     * 특정 사용자의 모든 디바이스 ID 조회
     */
    public List<String> findAllDeviceIdsByUserId(Long userId) {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + userId + ":*");

        return keys.stream()
                .map(key -> key.substring(key.lastIndexOf(":") + 1))
                .collect(Collectors.toList());
    }

    /**
     * 가장 오래된 디바이스 삭제 (TTL이 가장 짧은 것)
     */
    public void deleteOldestToken(Long userId) {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + userId + ":*");
        if (keys.isEmpty()) {
            return;
        }

        // TTL이 가장 짧은 키 찾기
        String oldestKey = null;
        long minTtl = Long.MAX_VALUE;

        for (String key : keys) {
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl < minTtl) {
                minTtl = ttl;
                oldestKey = key;
            }
        }

        if (oldestKey != null) {
            stringRedisTemplate.delete(oldestKey);
            log.debug("가장 오래된 RefreshToken 삭제 - Key: {}", oldestKey);
        }
    }
}
