package com.back.product.adapter.in.event;

import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.kafka.ProductDeletedPayload;
import com.back.product.event.kafka.ProductUpdatedPayload;
import com.back.product.event.spring.ProductCreationCompletedEvent;
import com.back.product.event.spring.ProductDeletionCompletedEvent;
import com.back.product.event.spring.ProductUpdateCompletedEvent;
import com.back.product.mapper.ProductPayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductOutboxListener {
    private final ProductPayloadMapper productPayloadMapper;

    private final ProductOutboxUseCase productOutboxUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductCreation(ProductCreationCompletedEvent event) {
        ProductCreatedPayload payload = productPayloadMapper.toCreatedPayload(event.productInfoDto(), event.optionDtos(), event.thumbnailUrl());

        String eventId = event.eventId();
        String eventType = event.getClass().getSimpleName();
        String aggregateId = String.valueOf(event.productInfoDto().productInfoId());

        productOutboxUseCase.record(eventId, eventType, aggregateId, payload);

        log.info("[ProductOutboxListener] handleProductCreation payload: {}", payload);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductUpdate(ProductUpdateCompletedEvent event) {
        ProductUpdatedPayload payload = productPayloadMapper.toUpdatedPayload(event.productInfoDto(), event.optionDtos(), event.thumbnailUrl());

        String eventId = event.eventId();
        String eventType = event.getClass().getSimpleName();
        String aggregateId = String.valueOf(event.productInfoDto().productInfoId());

        productOutboxUseCase.record(eventId, eventType, aggregateId, payload);

        log.info("[ProductOutboxListener] handleProductUpdate payload: {}", payload);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductDelete(ProductDeletionCompletedEvent event) {
        ProductDeletedPayload payload = productPayloadMapper.toDeletedPayload(event.productInfoId());

        String eventId = event.eventId();
        String eventType = event.getClass().getSimpleName();
        String aggregateId = String.valueOf(event.productInfoId());

        productOutboxUseCase.record(eventId, eventType, aggregateId, payload);

        log.info("[ProductOutboxListener] handleProductDelete payload: {}", payload);
    }
}
