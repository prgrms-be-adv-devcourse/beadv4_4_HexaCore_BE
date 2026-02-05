package com.back.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import com.back.chat.adapter.out.outbox.ChatOutboxRelay;
import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.adapter.out.outbox.OutboxStatus;
import com.back.chat.event.ChatEventType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "chat.outbox.relay.processing-timeout-seconds=5"
})
class OutboxRecoverStuckProcessingTest {

    @Autowired ChatOutboxRepository chatOutboxRepository;
    @Autowired ChatOutboxRelay chatOutboxRelay;

    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    void recoverStuckProcessing_shouldMoveOldProcessingToFailed_andSetNextAttemptAtNow() {
        // given
        long timeoutSeconds = (long) ReflectionTestUtils.getField(chatOutboxRelay, "processingTimeoutSeconds");
        System.out.println("processingTimeoutSeconds = " + timeoutSeconds); // 5 찍혀야 정상

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = now.minusSeconds(timeoutSeconds + 10); // timeout 보다 오래된 processing

        ChatOutbox stuck = ChatOutbox.pending(
                UUID.randomUUID(),
                "MESSAGE_BLINDED",
                100L,
                ChatEventType.MESSAGE_BLINDED,
                "{\"roomId\":1,\"messageId\":100,\"reason\":\"REPORT_3\"}",
                now.minusMinutes(1)
        );

        // ✅ 도메인 메서드로 PROCESSING 만들기 (이 메서드는 네 엔티티에 추가해줘)
        stuck.markProcessing(startedAt);

        // nextAttemptAt이 있어도 now로 덮이는지 보고 싶으면:
        stuck.setNextAttemptAt(now.plusMinutes(10)); // (setter 싫으면 이것도 메서드로)
        // stuck.setRetryCount(0);

        Long id = chatOutboxRepository.saveAndFlush(stuck).getId();
        em.clear();

        // when
        chatOutboxRelay.recoverStuckProcessing();
        em.flush();
        em.clear();

        // then
        ChatOutbox reloaded = chatOutboxRepository.findById(id).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(reloaded.getProcessingStartedAt()).isNull();
        assertThat(reloaded.getLastError()).contains("processing timeout");
        assertThat(reloaded.getNextAttemptAt()).isNotNull();
    }
}

