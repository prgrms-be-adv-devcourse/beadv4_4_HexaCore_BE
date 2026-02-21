package com.back.detector.app;

import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.BidSpamDetectResult;
import com.back.detector.domain.DetectorPolicy;
import com.back.detector.domain.enums.DetectorRedisKey;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
    private static final String COUNT_KEY = DetectorRedisKey.BID_COUNT.getKey(USER_ID);
    private static final String BAN_KEY = DetectorRedisKey.BID_BAN.getKey(USER_ID);
    private static final String BAN_COUNT_KEY = DetectorRedisKey.BID_BAN_COUNT.getKey(USER_ID);
    private static final int MAX_ATTEMPTS = DetectorPolicy.BID_SPAM.getMaxAttempts();
    private static final int TIME_WINDOW   = DetectorPolicy.BID_SPAM.getTimeWindowMinutes();

    // --- null 방어 ---

    @Nested
    @DisplayName("userId가 null인 경우")
    class WhenUserIdIsNull {

        @Test
        @DisplayName("userId가 null이면 IllegalArgumentException을 던져야 한다")
        void should_throw_when_user_id_is_null() {
            assertThatThrownBy(() -> bidSpamDetector.checkBidSpam(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("userId는 null일 수 없습니다");

            verifyNoInteractions(detectorRedisTemplate);
        }
    }

    // --- 정상 흐름----

    @Nested
    @DisplayName("스팸이 아닌 정상 입찰 시나리오")
    class WhenBidCountIsWithinLimit {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(BAN_KEY)).thenReturn(null);
        }

        @Test
        @DisplayName("첫 번째 입찰 시 카운트가 1이면 예외 없이 통과해야 한다")
        void should_pass_on_first_bid() {
            when(valueOperations.increment(COUNT_KEY)).thenReturn(1L);

            assertThatCode(() -> bidSpamDetector.checkBidSpam(USER_ID)).doesNotThrowAnyException();

            verify(valueOperations).increment(COUNT_KEY);
            verify(detectorRedisTemplate).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("정확히 maxAttempts(5회)까지는 예외 없이 통과해야 한다")
        void should_pass_when_count_equals_max_attempts() {
            when(valueOperations.increment(COUNT_KEY)).thenReturn((long) MAX_ATTEMPTS);

            assertThatCode(() -> bidSpamDetector.checkBidSpam(USER_ID)).doesNotThrowAnyException();

            verify(valueOperations).increment(COUNT_KEY);
            verify(detectorRedisTemplate, never()).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }
    }

    // --- 스팸 감지 ---

    @Nested
    @DisplayName("스팸 감지 여부 검증")
    class WhenBidCountExceedsLimit {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(BAN_KEY)).thenReturn(null);
        }

        @Test
        @DisplayName("카운트가 maxAttempts(5)를 초과하면 DetectResult를 반환해야 한다")
        void should_return_detect_result_when_count_exceeds_max_attempts() {
            when(valueOperations.increment(COUNT_KEY)).thenReturn((long) MAX_ATTEMPTS + 1);
            when(valueOperations.increment(BAN_COUNT_KEY)).thenReturn(1L);

            BidSpamDetectResult result = bidSpamDetector.checkBidSpam(USER_ID);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("카운트가 매우 높아도(예: 100) DetectResult를 반환해야 한다")
        void should_return_detect_result_when_count_is_very_high() {
            when(valueOperations.increment(COUNT_KEY)).thenReturn(100L);
            when(valueOperations.increment(BAN_COUNT_KEY)).thenReturn(1L);

            BidSpamDetectResult result = bidSpamDetector.checkBidSpam(USER_ID);

            assertThat(result).isNotNull();
        }
    }

    // --- 이미 차단된 유저 ---

    @Nested
    @DisplayName("이미 차단된 유저의 동작")
    class WhenUserIsAlreadyBanned {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("차단 중인 유저이면 카운트 확인 없이 즉시 DetectResult를 반환해야 한다")
        void should_return_detect_result_immediately_when_banned() {
            when(valueOperations.get(BAN_KEY)).thenReturn(BidSpamBanLevel.FIRST.name());

            BidSpamDetectResult result = bidSpamDetector.checkBidSpam(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.banLevel()).isEqualTo(BidSpamBanLevel.FIRST);
            verify(valueOperations, never()).increment(COUNT_KEY);
        }

        @Test
        @DisplayName("차단 중인 유저는 requestCount가 0으로 반환되어야 한다")
        void should_return_zero_request_count_when_banned() {
            when(valueOperations.get(BAN_KEY)).thenReturn(BidSpamBanLevel.SECOND.name());

            BidSpamDetectResult result = bidSpamDetector.checkBidSpam(USER_ID);

            assertThat(result.requestCount()).isZero();
        }
    }

    // --- Redis 키 및 TTL 동작---

    @Nested
    @DisplayName("Redis 키 생성 및 TTL 설정 검증")
    class RedisKeyAndTtlBehavior {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("올바른 키 형식(bid:count:{userId})으로 increment가 호출되어야 한다")
        void should_use_correct_key_format() {
            Long targetUserId = 42L;
            String expectedCountKey = "bid:count:42";
            String expectedBanKey = "bid:ban:42";
            when(valueOperations.get(expectedBanKey)).thenReturn(null);
            when(valueOperations.increment(expectedCountKey)).thenReturn(1L);

            bidSpamDetector.checkBidSpam(targetUserId);

            verify(valueOperations).increment(expectedCountKey);
        }

        @Test
        @DisplayName("첫 번째 요청(count=1)일 때만 expire를 설정해야 한다")
        void should_set_expire_only_on_first_increment() {
            when(valueOperations.get(BAN_KEY)).thenReturn(null);
            when(valueOperations.increment(COUNT_KEY)).thenReturn(1L);

            bidSpamDetector.checkBidSpam(USER_ID);

            verify(detectorRedisTemplate).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("두 번째 이상 요청(count>1)일 때는 expire를 설정하지 않아야 한다")
        void should_not_set_expire_after_first_increment() {
            when(valueOperations.get(BAN_KEY)).thenReturn(null);
            when(valueOperations.increment(COUNT_KEY)).thenReturn(2L);

            bidSpamDetector.checkBidSpam(USER_ID);

            verify(detectorRedisTemplate, never()).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("스팸 감지 시 DetectResult를 반환하고 expire는 첫 카운트에만 호출되어야 한다")
        void should_not_call_expire_when_spam_detected_on_non_first_count() {
            when(valueOperations.get(BAN_KEY)).thenReturn(null);
            when(valueOperations.increment(COUNT_KEY)).thenReturn((long) MAX_ATTEMPTS + 1);
            when(valueOperations.increment(BAN_COUNT_KEY)).thenReturn(1L);

            BidSpamDetectResult result = bidSpamDetector.checkBidSpam(USER_ID);

            assertThat(result).isNotNull();
            verify(detectorRedisTemplate, never()).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }
    }

    // --- 다수 유저 독립성 ---

    @Nested
    @DisplayName("유저별 카운트 독립성")
    class UserIsolation {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("서로 다른 유저의 키가 독립적으로 생성되어야 한다")
        void should_use_different_keys_per_user() {
            Long userA = 1L;
            Long userB = 2L;
            when(valueOperations.get("bid:ban:1")).thenReturn(null);
            when(valueOperations.get("bid:ban:2")).thenReturn(null);
            when(valueOperations.increment("bid:count:1")).thenReturn(1L);
            when(valueOperations.increment("bid:count:2")).thenReturn(1L);

            bidSpamDetector.checkBidSpam(userA);
            bidSpamDetector.checkBidSpam(userB);

            verify(valueOperations).increment("bid:count:1");
            verify(valueOperations).increment("bid:count:2");
        }
    }
}
