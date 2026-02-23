package com.back.product.adapter.in.event;

import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.app.facade.ProductOutboxFacade;
import com.back.product.event.spring.ProductCreationCompletedEvent;
import com.back.product.event.spring.ProductDeletionCompletedEvent;
import com.back.product.event.spring.ProductUpdateCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ProductSpringEventListener {
    private final ProductKafkaEventPublisher eventPublisher;
    private final ProductOutboxFacade productOutboxFacade;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreation(ProductCreationCompletedEvent event) {
        productOutboxFacade.publish(event.eventId(), eventPublisher::sendCreatedEvent);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdate(ProductUpdateCompletedEvent event) {
        productOutboxFacade.publish(event.eventId(), eventPublisher::sendModifiedEvent);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDelete(ProductDeletionCompletedEvent event) {
        productOutboxFacade.publish(event.eventId(), eventPublisher::sendDeletedEvent);
    }
}
