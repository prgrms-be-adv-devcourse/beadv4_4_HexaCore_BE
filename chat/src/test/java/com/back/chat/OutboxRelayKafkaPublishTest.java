package com.back.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import com.back.chat.adapter.out.ChatOutboxRelay;
import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.adapter.out.outbox.OutboxStatus;
import com.back.chat.event.ChatEventType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class OutboxRelayKafkaPublishTest {

    @Autowired ChatOutboxRepository chatOutboxRepository;
    @Autowired ChatOutboxRelay chatOutboxRelay;

    @MockitoBean
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void relay_shouldPublishKafka_andMarkSent() {
        // given
        ChatOutbox outbox = ChatOutbox.pending(
                UUID.randomUUID(),               // eventId (business id)
                "MESSAGE_BLINDED",               // eventName/string
                1L,                              // aggregateId (여기선 messageId가 더 적절할 수도 있음)
                ChatEventType.MESSAGE_BLINDED,
                "{\"roomId\":1,\"messageId\":100,\"reason\":\"REPORT_3\"}",
                LocalDateTime.now()
        );

        ChatOutbox saved = chatOutboxRepository.save(outbox);

        // 오버로드 문제 피하려고 doReturn 사용
        doReturn(null).when(kafkaTemplate).send(anyString(), anyString(), anyString());

        // when
        chatOutboxRelay.relayOnce();

        // then: kafka publish
        verify(kafkaTemplate, atLeastOnce())
                .send(anyString(), anyString(), contains("\"messageId\":100"));

        // then: status SENT
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ChatOutbox reloaded = chatOutboxRepository.findById(saved.getId()).orElseThrow();
                    assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.SENT);
                });
    }
}
