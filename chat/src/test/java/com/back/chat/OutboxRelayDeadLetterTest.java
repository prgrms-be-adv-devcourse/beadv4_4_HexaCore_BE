package com.back.chat;

import com.back.chat.adapter.out.ChatOutboxRelay;
import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.adapter.out.outbox.OutboxStatus;
import com.back.chat.event.ChatEventType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "chat.outbox.relay.max-retry=1",  // 여기서 핵심: 1회 실패 후 다음 시도에 바로 DEAD 처리되게
        "chat.outbox.relay.retry-base-delay-seconds=1",
        "chat.outbox.relay.retry-max-delay-seconds=1",
        "chat.kafka.topics.message-blinded=chat.message.blinded.v1",
        "chat.kafka.topics.message-blinded-dlt=chat.message.blinded.v1.dlt"
})
class OutboxRelayDeadLetterTest {

    @Autowired
    ChatOutboxRepository chatOutboxRepository;
    @Autowired
    ChatOutboxRelay chatOutboxRelay;
    @Autowired
    TransactionTemplate tx;
    @Autowired
    EntityManager em;

    @MockitoBean
    KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        tx.executeWithoutResult(s -> {
            chatOutboxRepository.deleteAll();
            em.flush();
            em.clear();
        });
        reset(kafkaTemplate);
    }

    @Test
    void relay_exceedMaxRetry_shouldPublishDlt_andBeDead() {
        Long outboxId = tx.execute(status -> {
            ChatOutbox saved = chatOutboxRepository.save(
                    ChatOutbox.pending(
                            UUID.randomUUID(),
                            "MESSAGE_BLINDED",
                            100L,
                            ChatEventType.MESSAGE_BLINDED,
                            "{\"roomId\":1,\"messageId\":100,\"reason\":\"REPORT_3\"}",
                            LocalDateTime.now()
                    )
            );
            em.flush(); em.clear();
            return saved.getId();
        });

        // send는 계속 실패하도록
        CompletableFuture<SendResult<String, String>> fail = new CompletableFuture<>();
        fail.completeExceptionally(new RuntimeException("boom"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(fail);

        // 1) 첫 relay: 실패 -> FAILED(retryCount=1)
        chatOutboxRelay.relayOnce();

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> tx.executeWithoutResult(s -> {
            ChatOutbox r = chatOutboxRepository.findById(outboxId).orElseThrow();
            System.out.println("status=" + r.getStatus() + ", retry=" + r.getRetryCount() + ", next=" + r.getNextAttemptAt());
        }));

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> tx.executeWithoutResult(s -> {
            ChatOutbox r = chatOutboxRepository.findById(outboxId).orElseThrow();
            assertThat(r.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(r.getRetryCount()).isEqualTo(1);
        }));

        // 2) 재시도 가능하게 nextAttemptAt 과거로
        forceNextAttemptAtPast(outboxId);

        // 3) 두번째 relay: maxRetry 초과 처리 -> DLT + DEAD (네 구현 기준)
        chatOutboxRelay.relayOnce();

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> tx.executeWithoutResult(s -> {
            ChatOutbox r = chatOutboxRepository.findById(outboxId).orElseThrow();
            System.out.println("status=" + r.getStatus() + ", retry=" + r.getRetryCount() + ", next=" + r.getNextAttemptAt());
        }));

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> tx.executeWithoutResult(s -> {
            ChatOutbox r = chatOutboxRepository.findById(outboxId).orElseThrow();
            assertThat(r.getStatus()).isEqualTo(OutboxStatus.DEAD);
        }));

        // DLT 발행 검증 (구현이 "DEAD 처리 시점에 DLT 발행"이면 여기서 잡힘)
        verify(kafkaTemplate, atLeastOnce()).send(
                eq("chat.message.blinded.v1.dlt"),
                anyString(),
                contains("\"messageId\":100")
        );
    }

    private void forceNextAttemptAtPast(Long id) {
        tx.executeWithoutResult(s -> {
            em.createNativeQuery("""
                update chat_outbox
                set next_attempt_at = :t
                where id = :id
            """)
                    .setParameter("t", LocalDateTime.now().minusSeconds(1))
                    .setParameter("id", id)
                    .executeUpdate();
            em.flush();
            em.clear();
        });
    }
}
