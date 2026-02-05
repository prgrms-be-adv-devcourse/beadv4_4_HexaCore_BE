package com.back.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.back.chat.adapter.out.outbox.ChatOutboxRelay;
import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.adapter.out.outbox.OutboxStatus;
import com.back.chat.event.ChatEventType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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
                UUID.randomUUID(),
                "MESSAGE_BLINDED",
                1L,
                ChatEventType.MESSAGE_BLINDED,
                "{\"roomId\":1,\"messageId\":100,\"reason\":\"REPORT_3\"}",
                LocalDateTime.now()
        );

        // 트랜잭션/flush 문제 없게 saveAndFlush 사용
        ChatOutbox saved = chatOutboxRepository.saveAndFlush(outbox);

        // ✅ send(...)는 null이 아니라 future를 리턴해야 함 (relay 코드에서 .get(...) 하기 때문)
        CompletableFuture<SendResult<String, String>> ok =
                CompletableFuture.completedFuture(mock(SendResult.class));

        // 가장 흔히 타는 오버로드
        doReturn(ok).when(kafkaTemplate).send(anyString(), anyString(), anyString());

        // 혹시 Object 오버로드 타도 안전하게 (캐스팅 주의)
        doReturn((CompletableFuture) ok).when(kafkaTemplate).send(anyString(), any(), any());

        // when
        chatOutboxRelay.relayOnce();

        // then: kafka publish payload 검증
        verify(kafkaTemplate, atLeastOnce()).send(
                anyString(),
                anyString(),
                argThat(payload ->
                        payload != null && payload.matches("(?s).*\"messageId\"\\s*:\\s*100.*")
                )
        );

        // then: status SENT
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ChatOutbox reloaded = chatOutboxRepository.findById(saved.getId()).orElseThrow();
                    System.out.println("lastError = " + reloaded.getLastError());
                    assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.SENT);
                });
    }
}
