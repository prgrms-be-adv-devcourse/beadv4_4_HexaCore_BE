package com.back.settlement.adapter.out;

import com.back.settlement.domain.outbox.SettlementOutboxEvent;
import com.back.settlement.domain.outbox.SettlementOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SettlementOutboxStateService {
    private final SettlementOutboxRepository outboxRepository;

    public SettlementOutboxStateService(SettlementOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public List<Long> markPending(int batchSize) {
        List<Long> ids = outboxRepository.findPendingIds(SettlementOutboxStatus.PENDING.name(), batchSize);
        if (!ids.isEmpty()) {
            outboxRepository.findAllById(ids).forEach(SettlementOutboxEvent::markAsProcessing);
        }
        return ids;
    }

    @Transactional
    public List<Long> markFailed(int batchSize, int maxRetry) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> ids = outboxRepository.findFailedIds(SettlementOutboxStatus.FAILED.name(), now, batchSize, maxRetry);
        if (!ids.isEmpty()) {
            outboxRepository.findAllById(ids).forEach(SettlementOutboxEvent::markAsProcessing);
        }
        return ids;
    }

    @Transactional
    public int markTimedOutProcessingAsPending(int processingTimeoutMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(processingTimeoutMinutes);
        List<Long> ids = outboxRepository.findTimedOutProcessingIds(SettlementOutboxStatus.PROCESSING.name(), threshold);
        outboxRepository.findAllById(ids).forEach(SettlementOutboxEvent::markAsPending);
        return ids.size();
    }
}
