package com.back.product.adapter.in.event;

import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.dto.event.kafka.ProductCreatedPayload;
import com.back.product.dto.event.kafka.ProductDeletedPayload;
import com.back.product.dto.event.kafka.ProductUpdatedPayload;
import com.back.product.dto.event.spring.ProductCreationCompletedEvent;
import com.back.product.dto.event.spring.ProductDeletionCompletedEvent;
import com.back.product.dto.event.spring.ProductUpdateCompletedEvent;
import com.back.product.mapper.ProductPayloadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ProductSpringEventListener {
    private final ProductKafkaEventPublisher eventPublisher;
    private final ProductPayloadMapper productPayloadMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreation(ProductCreationCompletedEvent event) {
        ProductCreatedPayload payload = productPayloadMapper.toCreatedPayload(event.productInfoDto(), event.optionDtos(), event.thumbnailUrl());

        eventPublisher.sendCreatedEvent(payload);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdate(ProductUpdateCompletedEvent event) {
        ProductUpdatedPayload payload = productPayloadMapper.toUpdatedPayload(event.productInfoDto(), event.optionDtos(), event.thumbnailUrl());

        eventPublisher.sendModifiedEvent(payload);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDelete(ProductDeletionCompletedEvent event) {
        ProductDeletedPayload payload = productPayloadMapper.toDeletedPayload(event.productInfoId());

        eventPublisher.sendDeletedEvent(payload);
    }
}
