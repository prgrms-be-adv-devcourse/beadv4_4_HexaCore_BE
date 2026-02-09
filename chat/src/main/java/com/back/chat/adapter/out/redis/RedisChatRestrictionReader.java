package com.back.chat.adapter.out.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RedisChatRestrictionReader {

    private static final String KEY_PREFIX = "chat:restrictedUntil:";
    private final StringRedisTemplate redis;

    public LocalDateTime getRestrictedUntil(Long userId) {
        String v = redis.opsForValue().get(KEY_PREFIX + userId);
        if (v == null || v.isBlank()) return null;
        return LocalDateTime.parse(v); // user에서 toString()으로 저장했으면 OK
    }

    public void delete(Long userId) {
        redis.delete(KEY_PREFIX + userId);
    }
}

