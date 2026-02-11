package com.back.user.app;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RedisChatRestrictionCache {

    private static final String KEY_PREFIX = "chat:restrictedUntil:";

    private final StringRedisTemplate redis;

    public void put(Long userId, LocalDateTime restrictedUntil, LocalDateTime now) {
        Duration ttl = Duration.between(now, restrictedUntil);
        if (ttl.isZero() || ttl.isNegative()) {
            // 이미 만료된 제한이면 캐시 제거
            delete(userId);
            return;
        }

        String key = key(userId);
        redis.opsForValue().set(key, restrictedUntil.toString(), ttl);
    }

    public void delete(Long userId) {
        redis.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
