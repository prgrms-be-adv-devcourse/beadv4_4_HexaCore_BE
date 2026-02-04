package com.back.chat;

import com.back.chat.adapter.out.ChatOutboxRelay;
import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.adapter.out.outbox.OutboxStatus;
import com.back.chat.event.ChatEventType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
        "chat.outbox.relay.max-retry=1",
        "chat.outbox.relay.retry-base-delay-seconds=1",
        "chat.outbox.relay.retry-max-delay-seconds=1",
        "chat.kafka.topics.message-blinded=chat.message.blinded.v1",
        "chat.kafka.topics.message-blinded-dlt=chat.message.blinded.v1.dlt"
})
class OutboxRelayDeadLetterTest {

    @Autowired ChatOutboxRepository chatOutboxRepository;
    @Autowired ChatOutboxRelay chatOutboxRelay;
    @Autowired TransactionTemplate tx;
    @Autowired EntityManager em;

    @MockitoBean KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper om = new ObjectMapper();

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
    void relay_exceedMaxRetry_shouldPublishDlt_andBeDead() throws Exception {
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

        // ✅ 정상 토픽은 실패, DLT 토픽은 성공
        CompletableFuture<SendResult<String, String>> fail = new CompletableFuture<>();
        fail.completeExceptionally(new RuntimeException("boom"));
        CompletableFuture<SendResult<String, String>> ok = CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenAnswer(inv -> {
                    String topic = inv.getArgument(0, String.class);
                    if (topic.endsWith(".dlt")) return ok;
                    return fail;
                });

        // 1) 첫 relay: 정상 발행 실패 -> FAILED(retry=1)
        chatOutboxRelay.relayOnce();

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> tx.executeWithoutResult(s -> {
            ChatOutbox r = chatOutboxRepository.findById(outboxId).orElseThrow();
            assertThat(r.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(r.getRetryCount()).isEqualTo(1);
        }));

        // 2) 재시도 가능하게 nextAttemptAt 과거로
        forceNextAttemptAtPast(outboxId);

        // 3) 두번째 relay: 정상 발행 실패 -> retry=2 -> DLT 발행 성공 -> DEAD
        chatOutboxRelay.relayOnce();

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> tx.executeWithoutResult(s -> {
            ChatOutbox r = chatOutboxRepository.findById(outboxId).orElseThrow();
            assertThat(r.getStatus()).isEqualTo(OutboxStatus.DEAD);
            assertThat(r.getRetryCount()).isEqualTo(2);
        }));

        // ✅ DLT 발행 payload를 캡처해서 JSON 파싱 후 messageId=100 검증
        ArgumentCaptor<String> dltJsonCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, atLeastOnce()).send(
                eq("chat.message.blinded.v1.dlt"),
                anyString(),
                dltJsonCaptor.capture()
        );

        String dltJson = dltJsonCaptor.getValue();
        JsonNode root = om.readTree(dltJson);

        // originalPayload는 "문자열로 된 JSON"이라 한 번 더 파싱
        String originalPayload = root.get("originalPayload").asText();
        JsonNode original = om.readTree(originalPayload);

        assertThat(original.get("messageId").asLong()).isEqualTo(100L);
        assertThat(root.get("eventType").asText()).isEqualTo("MESSAGE_BLINDED");
        assertThat(root.get("retryCount").asInt()).isEqualTo(2);
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


