package com.back.product.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.EventConsumptionLogRepository;
import com.back.product.domain.EventConsumptionLog;
import com.back.product.dto.command.EventConsumptionCommand;
import com.back.product.dto.enums.EventConsumptionStatus;
import com.back.product.mapper.EventConsumptionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLogUseCase {
    private final EventConsumptionLogRepository eventConsumptionLogRepository;
    private final EventConsumptionLogMapper eventConsumptionLogMapper;

    private static final long PROCESSING_TIMEOUT_MINUTES = 30;

    @Loggable
    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String eventId) {
        return eventConsumptionLogRepository.findByEventId(eventId)
                .map(this::isAlreadyProcessed)
                .orElse(false);
    }

    @Loggable
    @Transactional
    public void startProcessing(EventConsumptionCommand command) {
        eventConsumptionLogRepository.findByEventId(command.eventId())
                .ifPresentOrElse(
                        log -> {
                            if (isAlreadyProcessed(log)) {
                                throw new IllegalStateException("Already " + log.getStatus() + " event: " + command.eventId());
                            }
                            log.updateStatus(EventConsumptionStatus.PROCESSING);
                            log.incrementRetryCount();
                            eventConsumptionLogRepository.saveAndFlush(log);
                        },
                        () -> {
                            EventConsumptionLog eventLog = eventConsumptionLogMapper.toEntity(command);
                            eventConsumptionLogRepository.saveAndFlush(eventLog);
                        }
                );
    }

    @Loggable
    @Transactional
    public void markAsSuccess(String eventId) {
        eventConsumptionLogRepository.findByEventId(eventId)
                .ifPresent(log -> log.updateStatus(EventConsumptionStatus.SUCCEEDED));
    }

    @Loggable
    @Transactional
    public void markAsFailed(String eventId, String errorMessage) {
        eventConsumptionLogRepository.findByEventId(eventId)
                .ifPresent(log -> {
                    log.updateStatus(EventConsumptionStatus.FAILED);
                    log.updateErrorMessage(errorMessage);
                });
    }

    @Loggable
    @Transactional
    public void markAsDead(String eventId, String errorMessage) {
        eventConsumptionLogRepository.findByEventId(eventId)
                .ifPresent(
                        log -> {
                            log.updateStatus(EventConsumptionStatus.DLQ);
                            log.updateErrorMessage(errorMessage);
                        }
                );
    }

    private boolean isAlreadyProcessed(EventConsumptionLog log) {
        if (log.getStatus() == EventConsumptionStatus.SUCCEEDED) {
            return true;
        }
        
        if (log.getStatus() == EventConsumptionStatus.PROCESSING) {
            // 처리 시작 후 일정 시간(30분)이 지나지 않았다면 아직 처리 중으로 간주하여 차단
            return log.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(PROCESSING_TIMEOUT_MINUTES));
        }
        
        return false;
    }
}
