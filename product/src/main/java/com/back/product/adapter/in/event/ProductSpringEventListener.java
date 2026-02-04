package com.back.product.adapter.in.event;

import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.global.event.ProductCreationCompletedEvent;
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
}
