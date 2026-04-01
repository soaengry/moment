package com.soaengry.moment.domain.user.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OAuth2CodeRepository {

    private static final String KEY_PREFIX = "oauth2_code:";
    private static final long TTL_MINUTES = 5;
    private static final String DELIMITER = "::";

    private final StringRedisTemplate stringRedisTemplate;

    private String generateKey(String code) {
        return KEY_PREFIX + code;
    }

    /**
     * OAuth2 인증 완료 후 발급된 토큰을 일회용 코드와 함께 저장 (TTL 5분)
     */
    public void save(String code, String accessToken, String refreshToken) {
        String key = generateKey(code);
        String value = accessToken + DELIMITER + refreshToken;
        stringRedisTemplate.opsForValue().set(key, value, TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("OAuth2 일회용 코드 저장 - Key: {}", key);
    }

    /**
     * 코드로 토큰 조회 후 즉시 삭제 (1회성)
     */
    public String[] getAndDelete(String code) {
        String key = generateKey(code);
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        stringRedisTemplate.delete(key);
        log.debug("OAuth2 일회용 코드 사용 및 삭제 - Key: {}", key);
        return value.split(DELIMITER, 2);
    }
}
