package com.back.product.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.ProductOutboxEventRepository;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.mapper.ProductOutboxMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

@Service
@Validated
@RequiredArgsConstructor
public class ProductOutboxUseCase {
    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final ProductOutboxMapper productOutboxMapper;

    private final JsonMapper jsonMapper;

    @Loggable
    @Transactional
    public void record(@NotEmpty String eventId, @Valid ProductCreatedPayload payload) {
        String aggregateId = payload.productInfo().productInfoId().toString();
        String eventType = payload.getClass().getSimpleName();
        String payloadText = jsonMapper.writeValueAsString(payload);

        ProductOutboxEvent outboxEvent = productOutboxMapper.toOutboxEvent(eventId, aggregateId, eventType, payloadText);

        productOutboxEventRepository.save(outboxEvent);
    }

    @Loggable
    @Transactional(readOnly = true)
    public ProductOutboxEvent findRecordedEvent(@NotEmpty String eventId) {
        return productOutboxEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new CustomException(FailureCode.PRODUCT_OUTBOX_NOT_FOUND));
    }

    @Loggable
    @Transactional
    public void markAsSucceeded(@NotEmpty String eventId) {
        ProductOutboxEvent outbox = findRecordedEvent(eventId);

        outbox.markAsSucceeded();

        productOutboxEventRepository.save(outbox);
    }

    @Loggable
    @Transactional
    public void markAsFailed(@NotEmpty String eventId) {
        ProductOutboxEvent outbox = findRecordedEvent(eventId);

        outbox.markAsFailed();

        productOutboxEventRepository.save(outbox);
    }
}
