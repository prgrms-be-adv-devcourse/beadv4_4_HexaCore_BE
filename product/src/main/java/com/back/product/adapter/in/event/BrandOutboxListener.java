package com.back.product.adapter.in.event;

import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.dto.model.BrandDto;
import com.back.product.event.kafka.*;
import com.back.product.event.spring.BrandCreationCompletedEvent;
import com.back.product.event.spring.BrandUpdateCompletedEvent;
import com.back.product.mapper.BrandPayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandOutboxListener {
    private final BrandPayloadMapper brandPayloadMapper;

    private final ProductOutboxUseCase productOutboxUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleBrandCreation(BrandCreationCompletedEvent event) {
        BrandCreatedPayload payload = brandPayloadMapper.toCreatedPayload(event.brands());

        String eventId = event.eventId();
        String eventType = event.getClass().getSimpleName();
        String aggregateId = event.brands().stream()
                .map(BrandDto::name)
                .collect(Collectors.joining("-"));

        productOutboxUseCase.record(eventId, eventType, aggregateId, payload);

        log.info("[BrandOutboxListener] handleBrandCreation payload: {}", payload);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleBrandUpdate(BrandUpdateCompletedEvent event) {
        BrandUpdatedPayload payload = brandPayloadMapper.toUpdatePayload(event.brand());

        String eventId = event.eventId();
        String eventType = event.getClass().getSimpleName();
        String aggregateId = String.valueOf(event.brand().brandId());

        productOutboxUseCase.record(eventId, eventType, aggregateId, payload);

        log.info("[BrandOutboxListener] handleBrandUpdate payload: {}", payload);
    }
}
