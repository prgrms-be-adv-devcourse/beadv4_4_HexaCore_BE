package com.back.cash.domain.outbox;

import com.back.cash.domain.outbox.enums.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentOutboxTest {

    private static final String EVENT_ID = "test-event-id";
    private static final String TOPIC = "cash.payment.completed";
    private static final String PAYLOAD = "{\"header\":{},\"payload\":{}}";
    private static final int BASE_DELAY = 60;
    private static final int MAX_DELAY = 300;

    @Test
    @DisplayName("createPending - PENDING 상태로 생성, retryCount=0, createdAt 세팅")
    void createPending_initialState() {
        PaymentOutbox outbox = PaymentOutbox.createPending(EVENT_ID, TOPIC, PAYLOAD);

        assertThat(outbox.getEventId()).isEqualTo(EVENT_ID);
        assertThat(outbox.getTopic()).isEqualTo(TOPIC);
        assertThat(outbox.getPayload()).isEqualTo(PAYLOAD);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outbox.getRetryCount()).isZero();
        assertThat(outbox.getCreatedAt()).isNotNull();
        assertThat(outbox.getProcessedAt()).isNull();
        assertThat(outbox.getNextRetryAt()).isNull();
    }

    @Test
    @DisplayName("markAsSent - SENT 상태로 변경, processedAt 세팅")
    void markAsSent_changeStatusToSent() {
        PaymentOutbox outbox = PaymentOutbox.createPending(EVENT_ID, TOPIC, PAYLOAD);

        outbox.markAsSent();

        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(outbox.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsFailed - FAILED 상태로 변경, retryCount 증가, nextRetryAt 세팅")
    void markAsFailed_changeStatusAndIncrementRetryCount() {
        PaymentOutbox outbox = PaymentOutbox.createPending(EVENT_ID, TOPIC, PAYLOAD);
        LocalDateTime before = LocalDateTime.now();

        outbox.markAsFailed(BASE_DELAY, MAX_DELAY);

        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(outbox.getRetryCount()).isEqualTo(1);
        // 1번 실패: 2^0 * 60 = 60초 뒤
        assertThat(outbox.getNextRetryAt()).isAfterOrEqualTo(before.plusSeconds(59));
    }

    @Test
    @DisplayName("markAsFailed - 지수 백오프: 반복 실패 시 nextRetryAt이 점점 늘어남")
    void markAsFailed_exponentialBackoff() {
        PaymentOutbox outbox = PaymentOutbox.createPending(EVENT_ID, TOPIC, PAYLOAD);

        outbox.markAsFailed(BASE_DELAY, MAX_DELAY); // 1번: 60초
        LocalDateTime firstRetryAt = outbox.getNextRetryAt();

        outbox.markAsFailed(BASE_DELAY, MAX_DELAY); // 2번: 120초
        LocalDateTime secondRetryAt = outbox.getNextRetryAt();

        outbox.markAsFailed(BASE_DELAY, MAX_DELAY); // 3번: 240초
        LocalDateTime thirdRetryAt = outbox.getNextRetryAt();

        assertThat(outbox.getRetryCount()).isEqualTo(3);
        assertThat(secondRetryAt).isAfter(firstRetryAt);
        assertThat(thirdRetryAt).isAfter(secondRetryAt);
    }

    @Test
    @DisplayName("markAsFailed - 지수 백오프가 maxDelay를 초과하면 maxDelay로 고정")
    void markAsFailed_cappedAtMaxDelay() {
        PaymentOutbox outbox = PaymentOutbox.createPending(EVENT_ID, TOPIC, PAYLOAD);
        int smallMax = 50;

        outbox.markAsFailed(BASE_DELAY, smallMax); // 2^0 * 60 = 60 → cap 50
        LocalDateTime capped = outbox.getNextRetryAt();

        assertThat(capped).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(smallMax + 1));
    }
}
