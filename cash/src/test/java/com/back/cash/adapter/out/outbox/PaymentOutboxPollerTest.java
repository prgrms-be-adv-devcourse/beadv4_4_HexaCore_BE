package com.back.cash.adapter.out.outbox;

import com.back.cash.domain.outbox.PaymentCompletedOutbox;
import com.back.cash.domain.outbox.enums.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCompletedOutboxPollerTest {

    private PaymentCompletedOutboxPoller poller;

    @Mock
    private PaymentCompletedOutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> outboxKafkaTemplate;

    private static final String TOPIC = "cash.payment.completed";
    private static final String PAYLOAD = "{\"header\":{},\"payload\":{}}";

    @BeforeEach
    void setUp() throws Exception {
        poller = new PaymentCompletedOutboxPoller(outboxRepository, outboxKafkaTemplate);
        setField(poller, "pollSize", 100);
        setField(poller, "maxRetry", 10);
        setField(poller, "retryBaseDelaySeconds", 60);
        setField(poller, "retryMaxDelaySeconds", 300);
    }

    @Test
    @DisplayName("poll - PENDING 없고 재시도 대상도 없으면 Kafka 발행 안 함")
    void poll_whenNoPendingAndNoRetryable_doesNothing() {
        // given
        given(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Limit.class)))
                .willReturn(List.of());
        given(outboxRepository.findRetryable(anyInt(), any(), any(Limit.class)))
                .willReturn(List.of());

        // when
        poller.poll();

        // then
        verifyNoInteractions(outboxKafkaTemplate);
    }

    @Test
    @DisplayName("poll - PENDING 발행 성공 시 SENT 상태로 변경")
    void poll_whenSendSuccess_thenMarkAsSent() {
        // given
        PaymentCompletedOutbox outbox = PaymentCompletedOutbox.createPending("event-id-1", TOPIC, PAYLOAD);
        given(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Limit.class)))
                .willReturn(List.of(outbox));
        given(outboxRepository.findRetryable(anyInt(), any(), any(Limit.class)))
                .willReturn(List.of());
        given(outboxKafkaTemplate.send(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(null));

        // when
        poller.poll();

        // then
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(outbox.getProcessedAt()).isNotNull();
        verify(outboxKafkaTemplate).send(TOPIC, PAYLOAD);
    }

    @Test
    @DisplayName("poll - 발행 실패 시 FAILED 상태, retryCount 증가, nextRetryAt 세팅")
    void poll_whenSendFails_thenMarkAsFailed() {
        // given
        PaymentCompletedOutbox outbox = PaymentCompletedOutbox.createPending("event-id-2", TOPIC, PAYLOAD);
        given(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Limit.class)))
                .willReturn(List.of(outbox));
        given(outboxRepository.findRetryable(anyInt(), any(), any(Limit.class)))
                .willReturn(List.of());

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("Kafka 연결 실패"));
        given(outboxKafkaTemplate.send(anyString(), anyString())).willReturn(failed);

        // when
        poller.poll();

        // then
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(outbox.getRetryCount()).isEqualTo(1);
        assertThat(outbox.getNextRetryAt()).isNotNull();
    }

    @Test
    @DisplayName("poll - FAILED 재시도 대상 발행 성공 시 SENT 상태로 변경")
    void poll_whenRetryableSuccess_thenMarkAsSent() {
        // given
        PaymentCompletedOutbox retryable = PaymentCompletedOutbox.createPending("event-id-3", TOPIC, PAYLOAD);
        retryable.markAsFailed(60, 300);

        given(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Limit.class)))
                .willReturn(List.of());
        given(outboxRepository.findRetryable(anyInt(), any(), any(Limit.class)))
                .willReturn(List.of(retryable));
        given(outboxKafkaTemplate.send(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(null));

        // when
        poller.poll();

        // then
        assertThat(retryable.getStatus()).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    @DisplayName("poll - 여러 건 중 일부 실패해도 나머지는 정상 처리")
    void poll_whenPartialFailure_thenProcessesEachIndependently() {
        // given
        PaymentCompletedOutbox success = PaymentCompletedOutbox.createPending("event-id-1", TOPIC, PAYLOAD);
        PaymentCompletedOutbox fail = PaymentCompletedOutbox.createPending("event-id-2", TOPIC, PAYLOAD);

        given(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Limit.class)))
                .willReturn(List.of(success, fail));
        given(outboxRepository.findRetryable(anyInt(), any(), any(Limit.class)))
                .willReturn(List.of());

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka 연결 실패"));

        given(outboxKafkaTemplate.send(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(null))
                .willReturn(failedFuture);

        // when
        poller.poll();

        // then
        assertThat(success.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(fail.getStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("poll - FAILED 재시도가 또 실패하면 retryCount 누적, nextRetryAt 더 뒤로 밀림")
    void poll_whenRetryableFails_thenRetryCountAccumulatesAndNextRetryAtDelayed() {
        // given
        PaymentCompletedOutbox retryable = PaymentCompletedOutbox.createPending("event-id-4", TOPIC, PAYLOAD);
        retryable.markAsFailed(60, 300); // retryCount=1, nextRetryAt=+60s
        LocalDateTime firstNextRetryAt = retryable.getNextRetryAt();

        given(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Limit.class)))
                .willReturn(List.of());
        given(outboxRepository.findRetryable(anyInt(), any(), any(Limit.class)))
                .willReturn(List.of(retryable));

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka 연결 실패"));
        given(outboxKafkaTemplate.send(anyString(), anyString())).willReturn(failedFuture);

        // when
        poller.poll();

        // then
        assertThat(retryable.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(retryable.getRetryCount()).isEqualTo(2);
        assertThat(retryable.getNextRetryAt()).isAfter(firstNextRetryAt); // +60s → +120s
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
