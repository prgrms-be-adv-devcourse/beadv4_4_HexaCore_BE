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

    public boolean isRestricted(Long userId) {
        return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + userId));
    }
}

