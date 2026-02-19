package com.back.product.adapter.in.event;

import com.back.product.adapter.out.event.BrandKafkaEventPublisher;
import com.back.product.app.facade.ProductOutboxFacade;
import com.back.product.event.spring.BrandCreationCompletedEvent;
import com.back.product.event.spring.BrandDeletionCompletedEvent;
import com.back.product.event.spring.BrandUpdateCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandSpringEventListener {
    private final BrandKafkaEventPublisher eventPublisher;
    private final ProductOutboxFacade productOutboxFacade;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBrandCreation(BrandCreationCompletedEvent event) {
        productOutboxFacade.publish(event.eventId(), eventPublisher::sendCreatedEvent);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBrandUpdate(BrandUpdateCompletedEvent event) {
        productOutboxFacade.publish(event.eventId(), eventPublisher::sendUpdatedEvent);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBrandDeletion(BrandDeletionCompletedEvent event) {
        productOutboxFacade.publish(event.eventId(), eventPublisher::sendDeletedEvent);
    }
}
