package com.example.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSessionService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOGIN_SESSION_KEY_PREFIX = "login:session:";
    private static final long DEFAULT_SESSION_EXPIRE_HOURS = 24;

    public void saveLoginSession(String email, String accessToken, String refreshToken) {
        String key = LOGIN_SESSION_KEY_PREFIX + email;

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("email", email);
        sessionData.put("accessToken", accessToken);
        sessionData.put("refreshToken", refreshToken);
        sessionData.put("loginTime", LocalDateTime.now().toString());

        redisTemplate.opsForHash().putAll(key, sessionData);
        redisTemplate.expire(key, Duration.ofHours(DEFAULT_SESSION_EXPIRE_HOURS));

        log.info("Login session saved to Redis for user: {}", email);
    }

    public Map<Object, Object> getLoginSession(String email) {
        String key = LOGIN_SESSION_KEY_PREFIX + email;
        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(key);

        if (sessionData.isEmpty()) {
            log.warn("No login session found for user: {}", email);
            return null;
        }

        log.info("Login session retrieved from Redis for user: {}", email);
        return sessionData;
    }

    public void deleteLoginSession(String email) {
        String key = LOGIN_SESSION_KEY_PREFIX + email;
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("Login session deleted from Redis for user: {}", email);
        } else {
            log.warn("No login session to delete for user: {}", email);
        }
    }

    public boolean hasActiveSession(String email) {
        String key = LOGIN_SESSION_KEY_PREFIX + email;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void updateSessionExpiry(String email, long hours) {
        String key = LOGIN_SESSION_KEY_PREFIX + email;
        redisTemplate.expire(key, Duration.ofHours(hours));
        log.info("Session expiry updated for user: {} to {} hours", email, hours);
    }
}
