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
        chatOutboxRepository.markSent(outboxId, now);
    }

    @Transactional
    public void markFailed(Long outboxId, String error, int retryBaseDelaySeconds) {
        chatOutboxRepository.markInitialFailed(outboxId, error, retryBaseDelaySeconds);
    }

    @Transactional
    public String markFailedOrDead(
            Long outboxId,
            String error,
            LocalDateTime now
    ) {
        return chatOutboxRepository.markFailedOrDeadAtomic(
                outboxId,
                error,
                now,
                OutboxPollingProperties.MAX_RETRY,
                OutboxPollingProperties.RETRY_BASE_DELAY_SECONDS,
                OutboxPollingProperties.RETRY_MAX_DELAY_SECONDS
        );
    }
}
