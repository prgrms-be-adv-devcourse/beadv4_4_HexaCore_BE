package com.back.settlement.adapter.out;

import com.back.settlement.domain.outbox.SettlementOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class SettlementOutboxPoller {
    private final SettlementOutboxRepository outboxRepository;
    private final SettlementOutboxPublisher outboxPublisher;
    private final SettlementOutboxStateService outboxStateService;
    private final int batchSize;
    private final int maxRetry;
    private final int retentionDays;
    private final int processingTimeoutMinutes;

    public SettlementOutboxPoller(SettlementOutboxRepository outboxRepository,
                                  SettlementOutboxPublisher outboxPublisher,
                                  SettlementOutboxStateService outboxStateService,
                                  @Value("${settlement.outbox.polling.batch-size}") int batchSize,
                                  @Value("${settlement.outbox.polling.max-retry}") int maxRetry,
                                  @Value("${settlement.outbox.cleanup.retention-days}") int retentionDays,
                                  @Value("${settlement.outbox.polling.processing-timeout-minutes:5}") int processingTimeoutMinutes) {
        this.outboxRepository = outboxRepository;
        this.outboxPublisher = outboxPublisher;
        this.outboxStateService = outboxStateService;
        this.batchSize = batchSize;
        this.maxRetry = maxRetry;
        this.retentionDays = retentionDays;
        this.processingTimeoutMinutes = processingTimeoutMinutes;
    }

    /**
     * 주기적으로 PENDING 이벤트를 폴링하여 Kafka로 발행
     */
    @Scheduled(fixedDelayString = "${settlement.outbox.polling.fixed-delay-ms}")
    public void pollPending() {
        List<Long> outboxIds = outboxStateService.markPending(batchSize);
        if (outboxIds == null || outboxIds.isEmpty()) {
            return;
        }
        log.info("[OUTBOX] PENDING 이벤트 발행 시작. count={}", outboxIds.size());
        outboxPublisher.publish(outboxIds);
    }

    /**
     * 주기적으로 FAILED 이벤트를 폴링하여 Kafka로 재발행
     */
    @Scheduled(fixedDelayString = "${settlement.outbox.polling.fixed-delay-ms}")
    public void handleFailure() {
        List<Long> outboxIds = outboxStateService.markFailed(batchSize, maxRetry);
        if (outboxIds == null || outboxIds.isEmpty()) {
            return;
        }
        log.info("[OUTBOX] FAILED 이벤트 재발행 시작. count={}", outboxIds.size());
        outboxPublisher.publish(outboxIds);
    }

    /**
     * PROCESSING 상태에서 타임아웃된 이벤트를 PENDING으로 복구
     */
    @Scheduled(fixedDelayString = "${settlement.outbox.polling.recovery-fixed-delay-ms}")
    public void recoverStuckProcessing() {
        int recovered = outboxStateService.markTimedOutProcessingAsPending(processingTimeoutMinutes);
        if (recovered > 0) {
            log.warn("[OUTBOX] PROCESSING 타임아웃 복구. count={}, timeoutMinutes={}", recovered, processingTimeoutMinutes);
        }
    }

    /**
     * 발행 완료된 오래된 이벤트 삭제
     */
    @Scheduled(cron = "${settlement.outbox.cleanup.cron}")
    @Transactional
    public void deleteOldSentEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = outboxRepository.deleteOldSent(SettlementOutboxStatus.SENT, cutoff);
        log.info("[OUTBOX] 오래된 SENT 이벤트 삭제. count={}, retentionDays={}", deleted, retentionDays);
    }
}
