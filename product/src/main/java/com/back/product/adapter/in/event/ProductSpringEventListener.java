package com.back.product.adapter.in.event;

import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.spring.ProductCreationCompletedEvent;
import com.back.product.event.spring.ProductDeletionCompletedEvent;
import com.back.product.event.spring.ProductUpdateCompletedEvent;
import com.back.product.mapper.ProductPayloadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ProductSpringEventListener {
    private final ProductKafkaEventPublisher eventPublisher;
    private final ProductOutboxUseCase productOutboxUseCase;
    private final ProductPayloadMapper productPayloadMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreation(ProductCreationCompletedEvent event) {
        ProductOutboxEvent outbox = productOutboxUseCase.findRecordedEvent(event.eventId());

        eventPublisher.sendCreatedEvent(outbox);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdate(ProductUpdateCompletedEvent event) {
        ProductOutboxEvent outbox = productOutboxUseCase.findRecordedEvent(event.eventId());

        eventPublisher.sendModifiedEvent(outbox);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDelete(ProductDeletionCompletedEvent event) {
        ProductOutboxEvent outbox = productOutboxUseCase.findRecordedEvent(event.eventId());

        eventPublisher.sendDeletedEvent(outbox);
    }
}
