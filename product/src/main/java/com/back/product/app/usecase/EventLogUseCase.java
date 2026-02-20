package com.back.product.app.usecase;

import com.back.common.annotation.Loggable;
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

    @Loggable
    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String eventId) {
        return eventConsumptionLogRepository.findByEventId(eventId)
                .map(log -> isAlreadyProcessed(log.getStatus()))
                .orElse(false);
    }

    @Loggable
    @Transactional
    public void startProcessing(EventConsumptionCommand command) {
        eventConsumptionLogRepository.findByEventId(command.eventId())
                .ifPresentOrElse(
                        log -> {
                            if (isAlreadyProcessed(log.getStatus())) {
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

    private boolean isAlreadyProcessed(EventConsumptionStatus status) {
        return status == EventConsumptionStatus.SUCCEEDED || status == EventConsumptionStatus.PROCESSING;
    }
}
