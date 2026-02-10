package com.back.chat;


import com.back.chat.adapter.out.redis.RedisChatRestrictionReader;
import com.back.common.code.FailureCode;
import com.back.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class ChatRestrictionValidationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired StringRedisTemplate redis;
    @Autowired
    RedisChatRestrictionReader redisChatRestrictionReader;
    @Autowired ChatRestrictionValidator validator;

    private static final String KEY_PREFIX = "chat:restrictedUntil:";

    @AfterEach
    void tearDown() {
        redis.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void when_key_missing_should_pass() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        assertThatCode(() -> validator.validate(userId, now))
                .doesNotThrowAnyException();
    }

    @Test
    void when_expired_until_should_delete_and_pass() {
        Long userId = 2L;
        LocalDateTime now = LocalDateTime.now();

        String key = KEY_PREFIX + userId;
        LocalDateTime expiredUntil = now.minusSeconds(1);
        redis.opsForValue().set(key, expiredUntil.toString()); // TTL 없이 일부러 남겨둠

        assertThatCode(() -> validator.validate(userId, now))
                .doesNotThrowAnyException();

        // 만료 값은 삭제돼야 함
        assertThat(redis.opsForValue().get(key)).isNull();
    }

    @Test
    void when_future_until_should_throw_forbidden() {
        Long userId = 3L;
        LocalDateTime now = LocalDateTime.now();

        String key = KEY_PREFIX + userId;
        LocalDateTime futureUntil = now.plusMinutes(5);
        redis.opsForValue().set(key, futureUntil.toString());

        assertThatThrownBy(() -> validator.validate(userId, now))
                .isInstanceOf(ForbiddenException.class)
                .satisfies(ex -> {
                    ForbiddenException fe = (ForbiddenException) ex;
                    assertThat(fe.getFailureCode()).isEqualTo(FailureCode.CHAT_RESTRICTED);
                });

        // 유효한 제한이면 삭제되면 안 됨
        assertThat(redis.opsForValue().get(key)).isEqualTo(futureUntil.toString());
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        ChatRestrictionValidator chatRestrictionValidator(RedisChatRestrictionReader reader) {
            return new ChatRestrictionValidator(reader);
        }
    }

    @RequiredArgsConstructor
    static class ChatRestrictionValidator {
        private final RedisChatRestrictionReader redisChatRestrictionReader;

        public void validate(Long userId, LocalDateTime now) {
            LocalDateTime until = redisChatRestrictionReader.getRestrictedUntil(userId);
            if (until != null) {
                if (!until.isAfter(now)) {
                    redisChatRestrictionReader.delete(userId);
                } else {
                    throw new ForbiddenException(FailureCode.CHAT_RESTRICTED);
                }
            }
        }
    }
}
