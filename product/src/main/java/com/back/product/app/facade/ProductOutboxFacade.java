package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.dto.enums.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductOutboxFacade {
    private final ProductOutboxUseCase productOutboxUseCase;

    @Loggable
    public void publish(String eventId, Function<ProductOutboxEvent, CompletableFuture<?>> publisher) {
        if (!productOutboxUseCase.acquire(eventId)) {
            log.info("[ProductOutboxUseCase] {} Event is Processing.", eventId);
            return;
        }

        ProductOutboxEvent outbox = productOutboxUseCase.findRecordedEvent(eventId);

        try {
            publisher.apply(outbox).whenComplete((result, e) -> {
                if (e == null) {
                    productOutboxUseCase.complete(eventId, OutboxEventStatus.SUCCEEDED);
                    log.info("[ProductOutboxUseCase] {}-{} publish success.", outbox.getEventType(), outbox.getEventId());
                } else {
                    productOutboxUseCase.complete(eventId, OutboxEventStatus.FAILED);
                    log.info("[ProductOutboxUseCase] {}-{} publish failed: {}", outbox.getEventType(), outbox.getEventId(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            productOutboxUseCase.complete(eventId, OutboxEventStatus.FAILED);
            log.info("[ProductOutboxUseCase] {}-{} publish failed: {}", outbox.getEventType(), outbox.getEventId(), e.getMessage(), e);
            throw e;
        }
    }
}
