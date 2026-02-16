package com.back.product.adapter.in.event;

import com.back.product.adapter.out.event.BrandKafkaEventPublisher;
import com.back.product.event.kafka.BrandCreatedPayload;
import com.back.product.event.spring.BrandCreationCompletedEvent;
import com.back.product.mapper.BrandPayloadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class BrandSpringEventListener {
    private final BrandKafkaEventPublisher eventPublisher;
    private final BrandPayloadMapper brandPayloadMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreation(BrandCreationCompletedEvent event) {
        BrandCreatedPayload payload = brandPayloadMapper.toCreatedPayload(event.brands());

        eventPublisher.sendCreatedEvent(payload);
    }
}
