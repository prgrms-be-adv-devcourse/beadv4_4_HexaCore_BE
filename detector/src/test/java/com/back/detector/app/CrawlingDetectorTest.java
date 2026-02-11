package com.back.detector.app;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.DetectorPolicy;
import com.back.detector.domain.enums.DetectorRedisKey;
import com.back.detector.exception.CrawlingDetectedException;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlingDetectorTest {

    @Mock
    private RedisTemplate<String, String> detectorRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CrawlingDetector crawlingDetector;

    private static final String TEST_IP = "192.168.1.1";
    private static final String COUNT_KEY = DetectorRedisKey.CRAWLING_COUNT.getKey(TEST_IP);
    private static final String BAN_KEY = DetectorRedisKey.CRAWLING_BAN.getKey(TEST_IP);
    private static final String BAN_COUNT_KEY = BAN_KEY + ":count";
    private static final int MAX_ATTEMPTS = DetectorPolicy.CRAWLING.getMaxAttempts();
    private static final int TIME_WINDOW = DetectorPolicy.CRAWLING.getTimeWindowMinutes();

    @Nested
    @DisplayName("크롤링이 아닌 정상 조회 시나리오")
    class WhenViewCountIsWithinLimit {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("첫 번째 조회 시 카운트가 1이면 예외 없이 통과해야 한다")
        void should_pass_on_first_view() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(false);
            when(valueOperations.increment(COUNT_KEY)).thenReturn(1L);

            assertThatCode(() -> crawlingDetector.checkCrawling(TEST_IP)).doesNotThrowAnyException();

            verify(valueOperations).increment(COUNT_KEY);
            verify(detectorRedisTemplate).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("정확히 maxAttempts(100회)까지는 예외 없이 통과해야 한다")
        void should_pass_when_count_equals_max_attempts() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(false);
            when(valueOperations.increment(COUNT_KEY)).thenReturn((long) MAX_ATTEMPTS);

            assertThatCode(() -> crawlingDetector.checkCrawling(TEST_IP)).doesNotThrowAnyException();

            verify(valueOperations).increment(COUNT_KEY);
            verify(detectorRedisTemplate).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }
    }

    @Nested
    @DisplayName("크롤링 감지 여부 검증")
    class WhenViewCountExceedsLimit {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("카운트가 maxAttempts(100)를 초과하면 CrawlingDetectedException을 발생시켜야 한다")
        void should_throw_when_count_exceeds_max_attempts() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(false);
            when(valueOperations.increment(COUNT_KEY)).thenReturn((long) MAX_ATTEMPTS + 1);
            when(valueOperations.increment(BAN_COUNT_KEY)).thenReturn(1L);

            assertThatThrownBy(() -> crawlingDetector.checkCrawling(TEST_IP))
                    .isInstanceOf(CrawlingDetectedException.class);
        }

        @Test
        @DisplayName("카운트가 매우 높아도(예: 500) CrawlingDetectedException을 발생시켜야 한다")
        void should_throw_when_count_is_very_high() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(false);
            when(valueOperations.increment(COUNT_KEY)).thenReturn(500L);
            when(valueOperations.increment(BAN_COUNT_KEY)).thenReturn(1L);

            assertThatThrownBy(() -> crawlingDetector.checkCrawling(TEST_IP))
                    .isInstanceOf(CrawlingDetectedException.class);
        }
    }

    @Nested
    @DisplayName("이미 차단된 IP의 동작")
    class WhenIpIsAlreadyBanned {

        @Test
        @DisplayName("차단 중인 IP이면 카운트 확인 없이 즉시 예외를 발생시켜야 한다")
        void should_throw_immediately_when_banned() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(true);

            assertThatThrownBy(() -> crawlingDetector.checkCrawling(TEST_IP))
                    .isInstanceOf(CrawlingDetectedException.class);

            verify(valueOperations, never()).increment(COUNT_KEY);
        }
    }

    @Nested
    @DisplayName("차단 단계별 시간 검증")
    class BanLevelBehavior {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("첫 번째 차단 시 FIRST 단계(5분)로 차단되어야 한다")
        void should_apply_first_ban_level() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(false);
            when(valueOperations.increment(COUNT_KEY)).thenReturn((long) MAX_ATTEMPTS + 1);
            when(valueOperations.increment(BAN_COUNT_KEY)).thenReturn(1L);

            assertThatThrownBy(() -> crawlingDetector.checkCrawling(TEST_IP))
                    .isInstanceOf(CrawlingDetectedException.class);

            verify(detectorRedisTemplate).expire(BAN_KEY, CrawlingBanLevel.FIRST.getBanMinutes(), TimeUnit.MINUTES);
            verify(detectorRedisTemplate).delete(COUNT_KEY);
        }

        @Test
        @DisplayName("두 번째 차단 이상이면 SECOND 단계(1시간)로 차단되어야 한다")
        void should_apply_second_ban_level() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(false);
            when(valueOperations.increment(COUNT_KEY)).thenReturn((long) MAX_ATTEMPTS + 1);
            when(valueOperations.increment(BAN_COUNT_KEY)).thenReturn(2L);

            assertThatThrownBy(() -> crawlingDetector.checkCrawling(TEST_IP))
                    .isInstanceOf(CrawlingDetectedException.class);

            verify(detectorRedisTemplate).expire(BAN_KEY, CrawlingBanLevel.SECOND.getBanMinutes(), TimeUnit.MINUTES);
            verify(detectorRedisTemplate).delete(COUNT_KEY);
        }
    }

    @Nested
    @DisplayName("Redis 키 생성 및 TTL 설정 검증")
    class RedisKeyAndTtlBehavior {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("올바른 키 형식(crawl:count:{ip})으로 increment가 호출되어야 한다")
        void should_use_correct_count_key_format() {
            String targetIp = "10.0.0.1";
            String expectedCountKey = "crawl:count:10.0.0.1";
            when(detectorRedisTemplate.hasKey("crawl:ban:10.0.0.1")).thenReturn(false);
            when(valueOperations.increment(expectedCountKey)).thenReturn(1L);

            crawlingDetector.checkCrawling(targetIp);

            verify(valueOperations).increment(expectedCountKey);
        }

        @Test
        @DisplayName("increment 후 매번 expire를 갱신해야 한다 (sliding window)")
        void should_always_refresh_ttl_after_increment() {
            when(detectorRedisTemplate.hasKey(BAN_KEY)).thenReturn(false);
            when(valueOperations.increment(COUNT_KEY)).thenReturn(50L);

            crawlingDetector.checkCrawling(TEST_IP);

            verify(detectorRedisTemplate).expire(COUNT_KEY, TIME_WINDOW, TimeUnit.MINUTES);
        }
    }

    @Nested
    @DisplayName("IP별 카운트 독립성")
    class IpIsolation {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("서로 다른 IP의 키가 독립적으로 생성되어야 한다")
        void should_use_different_keys_per_ip() {
            String ipA = "10.0.0.1";
            String ipB = "10.0.0.2";
            when(detectorRedisTemplate.hasKey("crawl:ban:10.0.0.1")).thenReturn(false);
            when(detectorRedisTemplate.hasKey("crawl:ban:10.0.0.2")).thenReturn(false);
            when(valueOperations.increment("crawl:count:10.0.0.1")).thenReturn(1L);
            when(valueOperations.increment("crawl:count:10.0.0.2")).thenReturn(1L);

            crawlingDetector.checkCrawling(ipA);
            crawlingDetector.checkCrawling(ipB);

            verify(valueOperations).increment("crawl:count:10.0.0.1");
            verify(valueOperations).increment("crawl:count:10.0.0.2");
        }
    }
}
