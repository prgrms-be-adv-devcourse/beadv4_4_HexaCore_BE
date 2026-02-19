package com.back.product.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.event.KafkaPayload;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.ProductOutboxEventRepository;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.dto.enums.OutboxEventStatus;
import com.back.product.mapper.ProductOutboxMapper;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ProductOutboxUseCase {
    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final ProductOutboxMapper productOutboxMapper;

    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.outbox.expire}")
    private Long eventExpireMinutes;

    @Loggable
    @Transactional
    public void record(@NotEmpty String eventId, @NotEmpty String eventType, @NotEmpty String aggregateId, @NotNull KafkaPayload payload) {
        String payloadText = jsonMapper.writeValueAsString(payload);

        ProductOutboxEvent outboxEvent = productOutboxMapper.toOutboxEvent(eventId, aggregateId, eventType, payloadText);

        productOutboxEventRepository.save(outboxEvent);
    }

    @Loggable
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Boolean acquire(String eventId) {
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(eventExpireMinutes);

        int updatedRows = productOutboxEventRepository.updateStatus(eventId, OutboxEventStatus.PROCESSING, expiredAt);

        return updatedRows == 1;
    }

    @Loggable
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String eventId, OutboxEventStatus status) {
        ProductOutboxEvent outbox = findRecordedEvent(eventId);

        if (status.equals(OutboxEventStatus.SUCCEEDED)) {
            outbox.markAsSucceeded();
        } else {
            outbox.markAsFailed();
        }

        productOutboxEventRepository.save(outbox);
    }

    @Loggable
    @Transactional(readOnly = true)
    public ProductOutboxEvent findRecordedEvent(@NotEmpty String eventId) {
        return productOutboxEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new CustomException(FailureCode.PRODUCT_OUTBOX_NOT_FOUND));
    }
}
