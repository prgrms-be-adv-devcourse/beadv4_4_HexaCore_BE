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
    private static final String FLAG = "1";

    private final StringRedisTemplate redis;

    public void put(Long userId, LocalDateTime restrictedUntil, LocalDateTime now) {
        Duration ttl = Duration.between(now, restrictedUntil);
        if (ttl.isZero() || ttl.isNegative()) {
            delete(userId);
            return;
        }

        redis.opsForValue().set(key(userId), FLAG, ttl);
    }

    public boolean isRestricted(Long userId) {
        return Boolean.TRUE.equals(redis.hasKey(key(userId)));
    }

    public void delete(Long userId) {
        redis.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
