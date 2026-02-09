package com.back.product.adapter.in.event;

import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.dto.event.ProductCreationCompletedEvent;
import com.back.product.dto.event.ProductDeletionCompletedEvent;
import com.back.product.dto.event.ProductUpdateCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ProductSpringEventListener {
    private final ProductKafkaEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreation(ProductCreationCompletedEvent event) {
        eventPublisher.sendCreatedEvent(event.productInfoDto(), event.optionDtos(), event.thumbnailUrl());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdate(ProductUpdateCompletedEvent event) {
        eventPublisher.sendModifiedEvent(event.productInfoDto(), event.optionDtos(), event.thumbnailUrl());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDelete(ProductDeletionCompletedEvent event) {
        eventPublisher.sendDeletedEvent(event.productInfoId());
    }
}
