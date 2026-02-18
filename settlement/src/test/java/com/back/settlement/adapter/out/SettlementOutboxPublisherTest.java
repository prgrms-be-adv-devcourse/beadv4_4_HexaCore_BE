package com.back.settlement.adapter.out;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementOutboxPublisher 단위 테스트")
class SettlementOutboxPublisherTest {

    @Mock
    private SettlementOutboxEventSender outboxEventSender;

    private SettlementOutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SettlementOutboxPublisher(outboxEventSender);
    }

    @Test
    @DisplayName("id 목록을 순회하며 건별 worker를 호출한다")
    void publish_CallsWorkerForEachId() {
        List<Long> outboxIds = List.of(1L, 2L, 3L);

        publisher.publish(outboxIds);

        verify(outboxEventSender).send(1L);
        verify(outboxEventSender).send(2L);
        verify(outboxEventSender).send(3L);
    }

    @Test
    @DisplayName("대상이 없으면 worker를 호출하지 않는다")
    void publish_WhenNoOutboxIds_DoesNothing() {
        publisher.publish(List.of());

        verifyNoInteractions(outboxEventSender);
    }
}
