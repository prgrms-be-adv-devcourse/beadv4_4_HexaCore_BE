package com.back.chat.adapter.out.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxStatusUpdater {
    private final ChatOutboxRepository chatOutboxRepository;

    @Transactional
    public void markSent(Long outboxId, LocalDateTime now) {
        chatOutboxRepository.updateSent(outboxId, now);
    }

    @Transactional
    public void markFailed(Long outboxId, String error, LocalDateTime now,
                           int retryBaseDelaySeconds, int retryMaxDelaySeconds) {
        // nextAttemptAt 계산을 DB에서 하거나(추천) 자바에서 계산해서 넘겨도 됨
        chatOutboxRepository.updateFailed(outboxId, error, retryBaseDelaySeconds, retryMaxDelaySeconds);
    }

    @Transactional
    public void markDead(Long outboxId, String reason, LocalDateTime now) {
        chatOutboxRepository.updateDead(outboxId, reason, now);
    }
}
