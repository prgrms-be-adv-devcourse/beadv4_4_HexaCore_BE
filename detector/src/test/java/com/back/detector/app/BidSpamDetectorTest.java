package com.back.detector.app;

import com.back.detector.domain.DetectorPolicy;
import com.back.detector.domain.enums.DetectorRedisKey;
import com.back.detector.exception.BidSpamException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidSpamDetectorTest {

    @Mock
    private RedisTemplate<String, String> detectorRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private BidSpamDetector bidSpamDetector;

    private static final Long USER_ID = 100L;
    private static final String EXPECTED_KEY = DetectorRedisKey.BID_COUNT.getKey(USER_ID); // "bid:count:100"
    private static final int MAX_ATTEMPTS = DetectorPolicy.BID_SPAM.getMaxAttempts();      // 5
    private static final int TIME_WINDOW   = DetectorPolicy.BID_SPAM.getTimeWindowMinutes(); // 1

    @BeforeEach
    void setUp() {
        when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // --- 정상 흐름----

    @Nested
    @DisplayName("스팸이 아닌 정상 입찰 시나리오")
    class WhenBidCountIsWithinLimit {

        @Test
        @DisplayName("첫 번째 입찰 시 카운트가 1이면 예외 없이 통과해야 한다")
        void should_pass_on_first_bid() {
            when(valueOperations.increment(EXPECTED_KEY)).thenReturn(1L);

            assertThatCode(() -> bidSpamDetector.checkBidSpam(USER_ID)).doesNotThrowAnyException();

            verify(valueOperations).increment(EXPECTED_KEY);
            verify(detectorRedisTemplate).expire(EXPECTED_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("정확히 maxAttempts(5회)까지는 예외 없이 통과해야 한다")
        void should_pass_when_count_equals_max_attempts() {
            when(valueOperations.increment(EXPECTED_KEY)).thenReturn((long) MAX_ATTEMPTS);

            assertThatCode(() -> bidSpamDetector.checkBidSpam(USER_ID)).doesNotThrowAnyException(); // 예외 없음

            verify(valueOperations).increment(EXPECTED_KEY);
            verify(detectorRedisTemplate).expire(EXPECTED_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }
    }

    // --- 스팸 감지 ---

    @Nested
    @DisplayName("스팸 감지 여부 검증")
    class WhenBidCountExceedsLimit {

        @Test
        @DisplayName("카운트가 maxAttempts(5)를 초과하면 BidSpamException을 발생시켜야 한다")
        void should_throw_when_count_exceeds_max_attempts() {
            when(valueOperations.increment(EXPECTED_KEY)).thenReturn((long) MAX_ATTEMPTS + 1); // 6

            assertThatThrownBy(() -> bidSpamDetector.checkBidSpam(USER_ID))
                    .isInstanceOf(BidSpamException.class);
        }

        @Test
        @DisplayName("카운트가 매우 높아도(예: 100) BidSpamException을 발생시켜야 한다")
        void should_throw_when_count_is_very_high() {
            when(valueOperations.increment(EXPECTED_KEY)).thenReturn(100L);

            assertThatThrownBy(() -> bidSpamDetector.checkBidSpam(USER_ID))
                    .isInstanceOf(BidSpamException.class);
        }
    }

    // --- Redis 키 및 TTL 동작---

    @Nested
    @DisplayName("Redis 키 생성 및 TTL 설정 검증")
    class RedisKeyAndTtlBehavior {

        @Test
        @DisplayName("올바른 키 형식(bid:count:{userId})으로 increment가 호출되어야 한다")
        void should_use_correct_key_format() {
            Long targetUserId = 42L;
            String expectedKey = "bid:count:42";
            when(valueOperations.increment(expectedKey)).thenReturn(1L);

            bidSpamDetector.checkBidSpam(targetUserId);

            verify(valueOperations).increment(expectedKey);
        }

        @Test
        @DisplayName("increment 후 매번 expire를 갱신해야 한다 (slide window 방지)")
        void should_always_refresh_ttl_after_increment() {
            when(valueOperations.increment(EXPECTED_KEY)).thenReturn(3L);

            bidSpamDetector.checkBidSpam(USER_ID);

            verify(detectorRedisTemplate).expire(EXPECTED_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("스팸 감지되더라도 expire는 increment 직후 호출되어야 한다")
        void should_call_expire_even_when_spam_detected() {
            when(valueOperations.increment(EXPECTED_KEY)).thenReturn((long) MAX_ATTEMPTS + 1);

            assertThatThrownBy(() -> bidSpamDetector.checkBidSpam(USER_ID))
                    .isInstanceOf(BidSpamException.class);

            // expire는 스팸 체크 if-블록 이전에 실행되므로 반드시 호출됨
            verify(detectorRedisTemplate).expire(EXPECTED_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }
    }

    // --- 다수 유저 독립성 ---

    @Nested
    @DisplayName("유저별 카운트 독립성")
    class UserIsolation {

        @Test
        @DisplayName("서로 다른 유저의 키가 독립적으로 생성되어야 한다")
        void should_use_different_keys_per_user() {
            Long userA = 1L;
            Long userB = 2L;
            when(valueOperations.increment("bid:count:1")).thenReturn(1L);
            when(valueOperations.increment("bid:count:2")).thenReturn(1L);

            bidSpamDetector.checkBidSpam(userA);
            bidSpamDetector.checkBidSpam(userB);

            verify(valueOperations).increment("bid:count:1");
            verify(valueOperations).increment("bid:count:2");
        }
    }
}
