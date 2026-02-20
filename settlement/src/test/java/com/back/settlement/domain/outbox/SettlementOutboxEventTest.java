package com.back.settlement.domain.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SettlementOutboxEvent 도메인 테스트")
class SettlementOutboxEventTest {
    private static final String TOPIC = "settlement.payout.requested";
    private static final String PAYLOAD = """
            {"sellerId":100,"startAt":"2024-01-01T00:00:00","endAt":"2024-01-31T23:59:59","totalNetAmount":90000}
            """;

    @Test
    @DisplayName("createPending으로 생성 시 PENDING 초기 상태")
    void createPending_initialState() {
        SettlementOutboxEvent event = SettlementOutboxEvent.createPending(TOPIC, PAYLOAD);

        assertThat(event.getStatus()).isEqualTo(SettlementOutboxStatus.PENDING);
        assertThat(event.getTopic()).isEqualTo(TOPIC);
        assertThat(event.getPayload()).isEqualTo(PAYLOAD);
        assertThat(event.getRetryCount()).isZero();
        assertThat(event.getCreateDate()).isEqualTo(event.getUpdatedAt());
        assertThat(event.getNextAttemptAt()).isNull();
        assertThat(event.getSentDate()).isNull();
    }

    @Test
    @DisplayName("markAsProcessing 호출 시 PROCESSING 상태로 전환된다")
    void markAsProcessing_changesStatus() {
        SettlementOutboxEvent event = SettlementOutboxEvent.createPending(TOPIC, PAYLOAD);

        event.markAsProcessing();

        assertThat(event.getStatus()).isEqualTo(SettlementOutboxStatus.PROCESSING);
        assertThat(event.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsSent 호출 시 SENT 상태로 전환되고 sentDate가 설정된다")
    void markAsSent_changesStatusAndSetsSentDate() {
        SettlementOutboxEvent event = SettlementOutboxEvent.createPending(TOPIC, PAYLOAD);

        event.markAsSent();

        assertThat(event.getStatus()).isEqualTo(SettlementOutboxStatus.SENT);
        assertThat(event.getSentDate()).isNotNull();
        assertThat(event.getNextAttemptAt()).isNull();
    }

    @Test
    @DisplayName("markAsFailed 호출 시 FAILED 상태, retryCount 증가, nextAttemptAt 설정")
    void markAsFailed_changesStatusAndIncrementsRetry() {
        SettlementOutboxEvent event = SettlementOutboxEvent.createPending(TOPIC, PAYLOAD);
        LocalDateTime before = LocalDateTime.now();

        event.markAsFailed(5);

        assertThat(event.getStatus()).isEqualTo(SettlementOutboxStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getNextAttemptAt()).isAfter(before);
    }

    @Test
    @DisplayName("markAsPending 호출 시 PENDING 상태로 전환된다")
    void markAsPending_changesStatus() {
        SettlementOutboxEvent event = SettlementOutboxEvent.createPending(TOPIC, PAYLOAD);
        event.markAsProcessing();

        event.markAsPending();

        assertThat(event.getStatus()).isEqualTo(SettlementOutboxStatus.PENDING);
    }
}
