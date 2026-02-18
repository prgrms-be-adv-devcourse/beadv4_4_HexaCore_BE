package com.back.settlement.adapter.out;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.back.settlement.domain.outbox.SettlementOutboxEvent;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementOutboxEventSender 단위 테스트")
class SettlementOutboxEventSenderTest {

    @Mock
    private SettlementOutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private SettlementOutboxEventSender eventSender;

    @BeforeEach
    void setUp() {
        eventSender = new SettlementOutboxEventSender(outboxRepository, kafkaTemplate, 5);
    }

    @Test
    @DisplayName("발행 성공 시 outbox 상태를 SENT로 전이한다")
    void send_WhenKafkaSendSucceeds_MarksSent() {
        SettlementOutboxEvent outbox = mock(SettlementOutboxEvent.class);

        when(outbox.getId()).thenReturn(1L);
        when(outbox.getTopic()).thenReturn("topic");
        when(outbox.getPayload()).thenReturn("{\"payload\":true}");
        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));
        when(kafkaTemplate.send("topic", "{\"payload\":true}"))
                .thenReturn(CompletableFuture.completedFuture(null));

        eventSender.send(1L);

        verify(outbox).markAsSent();
        verify(outbox, never()).markAsFailed(anyInt());
    }

    @Test
    @DisplayName("발행 실패 시 outbox 상태를 FAILED로 전이한다")
    void send_WhenKafkaSendFails_MarksFailed() {
        SettlementOutboxEvent outbox = mock(SettlementOutboxEvent.class);
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("kafka down"));

        when(outbox.getId()).thenReturn(1L);
        when(outbox.getTopic()).thenReturn("topic");
        when(outbox.getPayload()).thenReturn("{\"payload\":true}");
        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));
        when(kafkaTemplate.send("topic", "{\"payload\":true}"))
                .thenReturn(failedFuture);

        eventSender.send(1L);

        verify(outbox).markAsFailed(5);
        verify(outbox, never()).markAsSent();
    }

    @Test
    @DisplayName("대상 outbox가 없으면 Kafka를 호출하지 않는다")
    void send_WhenOutboxNotFound_DoesNothing() {
        when(outboxRepository.findById(1L)).thenReturn(Optional.empty());

        eventSender.send(1L);

        verifyNoInteractions(kafkaTemplate);
    }
}
