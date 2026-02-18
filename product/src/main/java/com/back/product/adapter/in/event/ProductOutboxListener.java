package com.back.product.adapter.in.event;

import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.spring.ProductCreationCompletedEvent;
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

            productOutboxUseCase.record(event.eventId(), payload);

            log.info("[ProductOutboxListener] handleProductCreation payload: {}", payload);
        }
}
