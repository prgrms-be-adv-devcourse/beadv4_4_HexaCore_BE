package com.back.detector.app;

import com.back.detector.domain.HijackDetectResult;
import com.back.detector.domain.enums.DetectorRedisKey;
import com.back.detector.dto.HijackDetectedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HijackDetectorTest {

    @Mock
    private RedisTemplate<String, String> detectorRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private HijackDetector hijackDetector;

    private static final Long USER_ID = 100L;
    private static final String USER_EMAIL = "test@test.com";
    private static final String TRUSTED_IP = "192.168.1.1";
    private static final String NEW_IP = "10.0.0.1";
    private static final String TRUSTED_IP_SET_KEY = DetectorRedisKey.TRUSTED_IP.getKey(USER_ID);
    private static final String NEW_IP_TIMESTAMP_KEY = DetectorRedisKey.NEW_IP_TIMESTAMP.getKey(USER_ID, NEW_IP);
    private static final String NEW_IP_AMOUNT_KEY = DetectorRedisKey.NEW_IP_AMOUNT.getKey(USER_ID, NEW_IP);
    private static final BigDecimal WITHIN_LIMIT = new BigDecimal("100000");
    private static final BigDecimal EXCEEDS_LIMIT = new BigDecimal("250000");

    // --- 초기 IP 등록 ---

    @Nested
    @DisplayName("회원가입 시 초기 IP 등록")
    class WhenRegisterInitialIp {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForSet()).thenReturn(setOperations);
        }

        @Test
        @DisplayName("초기 IP가 신뢰 목록에 추가되어야 한다")
        void should_add_initial_ip_to_trusted_set() {
            hijackDetector.registerInitialIp(USER_ID, TRUSTED_IP);

            verify(setOperations).add(TRUSTED_IP_SET_KEY, TRUSTED_IP);
        }

        @Test
        @DisplayName("초기 IP 등록 시 TTL이 90일로 설정되어야 한다")
        void should_set_ttl_90_days_on_initial_ip_registration() {
            hijackDetector.registerInitialIp(USER_ID, TRUSTED_IP);

            verify(detectorRedisTemplate).expire(TRUSTED_IP_SET_KEY, 90, TimeUnit.DAYS);
        }
    }

    // --- 신뢰 IP 거래 ---

    @Nested
    @DisplayName("쿨다운이 종료된 신뢰 IP의 거래")
    class WhenTrustedIpWithNoCooldown {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForSet()).thenReturn(setOperations);
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(setOperations.isMember(TRUSTED_IP_SET_KEY, TRUSTED_IP)).thenReturn(true);
            when(valueOperations.get(DetectorRedisKey.NEW_IP_TIMESTAMP.getKey(USER_ID, TRUSTED_IP))).thenReturn(null);
        }

        @Test
        @DisplayName("어떤 금액이어도 null을 반환해야 한다 (차단 없음)")
        void should_return_null_for_any_amount() {
            HijackDetectResult result = hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, EXCEEDS_LIMIT);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("이벤트가 발행되지 않아야 한다")
        void should_not_publish_event() {
            hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, EXCEEDS_LIMIT);

            verify(applicationEventPublisher, never()).publishEvent(any());
        }
    }

    // --- 완전히 새로운 IP ---

    @Nested
    @DisplayName("완전히 새로운 IP의 첫 거래")
    class WhenCompletelyNewIp {

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.isMember(TRUSTED_IP_SET_KEY, NEW_IP)).thenReturn(false);
        }

        @Test
        @DisplayName("첫 거래가 한도 이하이면 IP를 신뢰 목록에 추가하고 null을 반환해야 한다")
        void should_add_ip_and_return_null_when_within_limit() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);

            HijackDetectResult result = hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, WITHIN_LIMIT);

            assertThat(result).isNull();
            verify(setOperations).add(TRUSTED_IP_SET_KEY, NEW_IP);
        }

        @Test
        @DisplayName("첫 거래가 한도 이하이면 쿨다운 타임스탬프가 저장되어야 한다")
        void should_store_cooldown_timestamp_when_within_limit() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, WITHIN_LIMIT);

            verify(valueOperations).set(eq(NEW_IP_TIMESTAMP_KEY), anyString(), eq(24L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("첫 거래가 한도 이하이면 초기 누적 금액이 저장되어야 한다")
        void should_store_initial_accumulated_amount_when_within_limit() {
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, WITHIN_LIMIT);

            verify(valueOperations).set(eq(NEW_IP_AMOUNT_KEY), eq(WITHIN_LIMIT.toString()), eq(24L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("첫 거래가 한도 초과이면 IP를 신뢰 목록에 추가하지 않아야 한다")
        void should_not_add_ip_when_first_transaction_exceeds_limit() {
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of());

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, EXCEEDS_LIMIT);

            verify(setOperations, never()).add(TRUSTED_IP_SET_KEY, NEW_IP);
        }

        @Test
        @DisplayName("첫 거래가 한도 초과이면 HijackDetectResult를 반환해야 한다")
        void should_return_detect_result_when_first_transaction_exceeds_limit() {
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of());

            HijackDetectResult result = hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, EXCEEDS_LIMIT);

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.userEmail()).isEqualTo(USER_EMAIL);
            assertThat(result.currentIp()).isEqualTo(NEW_IP);
            assertThat(result.transactionAmount()).isEqualTo(EXCEEDS_LIMIT);
        }

        @Test
        @DisplayName("첫 거래가 한도 초과이면 이벤트가 발행되어야 한다")
        void should_publish_event_when_first_transaction_exceeds_limit() {
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of());

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, EXCEEDS_LIMIT);

            verify(applicationEventPublisher).publishEvent(any(HijackDetectedEvent.class));
        }

        @Test
        @DisplayName("첫 거래 한도 초과 이벤트의 reason에 '첫 거래 한도 초과' 문구가 포함되어야 한다")
        void should_contain_first_transaction_exceeded_reason() {
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of());
            ArgumentCaptor<HijackDetectedEvent> captor = ArgumentCaptor.forClass(HijackDetectedEvent.class);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, EXCEEDS_LIMIT);

            verify(applicationEventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().reason()).contains("첫 거래 한도 초과");
        }
    }

    // --- 쿨다운 중 거래 ---

    @Nested
    @DisplayName("쿨다운 중인 IP의 누적 거래")
    class WhenNewIpInCooldown {

        private static final String TIMESTAMP = String.valueOf(System.currentTimeMillis());
        private static final String COOLDOWN_TIMESTAMP_KEY =
                DetectorRedisKey.NEW_IP_TIMESTAMP.getKey(USER_ID, TRUSTED_IP);
        private static final String COOLDOWN_AMOUNT_KEY =
                DetectorRedisKey.NEW_IP_AMOUNT.getKey(USER_ID, TRUSTED_IP);

        @BeforeEach
        void setUp() {
            when(detectorRedisTemplate.opsForSet()).thenReturn(setOperations);
            when(detectorRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(setOperations.isMember(TRUSTED_IP_SET_KEY, TRUSTED_IP)).thenReturn(true);
            when(valueOperations.get(COOLDOWN_TIMESTAMP_KEY)).thenReturn(TIMESTAMP);
        }

        @Test
        @DisplayName("누적 금액이 한도 이하이면 null을 반환해야 한다")
        void should_return_null_when_accumulated_within_limit() {
            when(valueOperations.get(COOLDOWN_AMOUNT_KEY)).thenReturn("50000");
            when(valueOperations.increment(eq(COOLDOWN_AMOUNT_KEY), anyLong())).thenReturn(150000L);

            HijackDetectResult result = hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, new BigDecimal("100000"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("누적 금액이 한도 이하이면 이벤트가 발행되지 않아야 한다")
        void should_not_publish_event_when_accumulated_within_limit() {
            when(valueOperations.get(COOLDOWN_AMOUNT_KEY)).thenReturn("50000");
            when(valueOperations.increment(eq(COOLDOWN_AMOUNT_KEY), anyLong())).thenReturn(150000L);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, new BigDecimal("100000"));

            verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("누적 금액이 한도를 초과하면 HijackDetectResult를 반환해야 한다")
        void should_return_detect_result_when_accumulated_exceeds_limit() {
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of(TRUSTED_IP));
            when(valueOperations.get(COOLDOWN_AMOUNT_KEY)).thenReturn("150000");
            when(valueOperations.increment(eq(COOLDOWN_AMOUNT_KEY), anyLong())).thenReturn(250000L);

            HijackDetectResult result = hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, new BigDecimal("100000"));

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.userEmail()).isEqualTo(USER_EMAIL);
        }

        @Test
        @DisplayName("누적 금액이 한도를 초과하면 이벤트가 발행되어야 한다")
        void should_publish_event_when_accumulated_exceeds_limit() {
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of(TRUSTED_IP));
            when(valueOperations.get(COOLDOWN_AMOUNT_KEY)).thenReturn("150000");
            when(valueOperations.increment(eq(COOLDOWN_AMOUNT_KEY), anyLong())).thenReturn(250000L);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, new BigDecimal("100000"));

            verify(applicationEventPublisher).publishEvent(any(HijackDetectedEvent.class));
        }

        @Test
        @DisplayName("누적 초과 이벤트의 reason에 '누적 금액 초과' 문구가 포함되어야 한다")
        void should_contain_accumulated_exceeded_reason() {
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of(TRUSTED_IP));
            when(valueOperations.get(COOLDOWN_AMOUNT_KEY)).thenReturn("150000");
            when(valueOperations.increment(eq(COOLDOWN_AMOUNT_KEY), anyLong())).thenReturn(250000L);
            ArgumentCaptor<HijackDetectedEvent> captor = ArgumentCaptor.forClass(HijackDetectedEvent.class);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, new BigDecimal("100000"));

            verify(applicationEventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().reason()).contains("누적 금액 초과");
        }

        @Test
        @DisplayName("쿨다운 중 거래 시 누적 금액 키에 increment가 호출되어야 한다")
        void should_increment_accumulated_amount_key() {
            when(valueOperations.get(COOLDOWN_AMOUNT_KEY)).thenReturn("50000");
            when(valueOperations.increment(eq(COOLDOWN_AMOUNT_KEY), anyLong())).thenReturn(150000L);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, TRUSTED_IP, new BigDecimal("100000"));

            verify(valueOperations).increment(eq(COOLDOWN_AMOUNT_KEY), eq(100000L));
        }
    }

    // --- 이벤트 내용 검증 ---

    @Nested
    @DisplayName("발행되는 이벤트 내용 검증")
    class EventPayloadVerification {

        @Test
        @DisplayName("이벤트에 userId, email, currentIp, transactionAmount가 올바르게 담겨야 한다")
        void should_publish_event_with_correct_payload() {
            when(detectorRedisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.isMember(TRUSTED_IP_SET_KEY, NEW_IP)).thenReturn(false);
            when(setOperations.members(TRUSTED_IP_SET_KEY)).thenReturn(Set.of(TRUSTED_IP));
            ArgumentCaptor<HijackDetectedEvent> captor = ArgumentCaptor.forClass(HijackDetectedEvent.class);

            hijackDetector.checkHijack(USER_ID, USER_EMAIL, NEW_IP, EXCEEDS_LIMIT);

            verify(applicationEventPublisher).publishEvent(captor.capture());
            HijackDetectedEvent event = captor.getValue();
            assertThat(event.userId()).isEqualTo(USER_ID);
            assertThat(event.email()).isEqualTo(USER_EMAIL);
            assertThat(event.currentIp()).isEqualTo(NEW_IP);
            assertThat(event.transactionAmount()).isEqualTo(EXCEEDS_LIMIT);
        }
    }

    // --- 유저별 독립성 ---

    @Nested
    @DisplayName("유저별 Redis 키 독립성")
    class UserIsolation {

        @Test
        @DisplayName("서로 다른 유저의 신뢰 IP 키가 독립적으로 조회되어야 한다")
        void should_use_different_trusted_ip_keys_per_user() {
            Long userA = 1L;
            Long userB = 2L;
            String keyA = DetectorRedisKey.TRUSTED_IP.getKey(userA);
            String keyB = DetectorRedisKey.TRUSTED_IP.getKey(userB);
            when(detectorRedisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.isMember(eq(keyA), anyString())).thenReturn(false);
            when(setOperations.isMember(eq(keyB), anyString())).thenReturn(false);
            when(setOperations.members(anyString())).thenReturn(Set.of());

            hijackDetector.checkHijack(userA, USER_EMAIL, NEW_IP, EXCEEDS_LIMIT);
            hijackDetector.checkHijack(userB, USER_EMAIL, NEW_IP, EXCEEDS_LIMIT);

            verify(setOperations).isMember(keyA, NEW_IP);
            verify(setOperations).isMember(keyB, NEW_IP);
        }
    }
}
