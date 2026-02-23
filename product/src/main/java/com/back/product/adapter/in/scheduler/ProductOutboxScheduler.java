package com.back.product.adapter.in.scheduler;

import com.back.product.adapter.out.event.BrandKafkaEventPublisher;
import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.app.facade.ProductOutboxFacade;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.spring.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOutboxScheduler {
    private final ProductOutboxFacade productOutboxFacade;
    private final ProductOutboxUseCase productOutboxUseCase;
    
    private final ProductKafkaEventPublisher productPublisher;
    private final BrandKafkaEventPublisher brandPublisher;

    @Value("${custom.kafka.outbox.retry.size}")
    private Long SIZE;

    @Value("${custom.kafka.outbox.retry.max-per-run}")
    private Long MAX_PER_RUN;

    private Map<String, Function<ProductOutboxEvent, CompletableFuture<?>>> publisherMap;

    @PostConstruct
    public void init() {
        publisherMap = new HashMap<>();
        // Product Publisher
        publisherMap.put(ProductCreationCompletedEvent.class.getSimpleName(), productPublisher::sendCreatedEvent);
        publisherMap.put(ProductUpdateCompletedEvent.class.getSimpleName(), productPublisher::sendModifiedEvent);
        publisherMap.put(ProductDeletionCompletedEvent.class.getSimpleName(), productPublisher::sendDeletedEvent);
        // Brand Publisher
        publisherMap.put(BrandCreationCompletedEvent.class.getSimpleName(), brandPublisher::sendCreatedEvent);
        publisherMap.put(BrandUpdateCompletedEvent.class.getSimpleName(), brandPublisher::sendUpdatedEvent);
        publisherMap.put(BrandDeletionCompletedEvent.class.getSimpleName(), brandPublisher::sendDeletedEvent);
    }

    @Scheduled(fixedDelayString = "${custom.kafka.outbox.retry.delay}")
    public void retryPendingEvents() {
        long totalProcessed = 0L;
        AtomicLong failedProcessed = new AtomicLong();

        while (totalProcessed < MAX_PER_RUN) {
            List<ProductOutboxEvent> eventIdsToRetry = productOutboxUseCase.findEventIds(SIZE);

            if (eventIdsToRetry.isEmpty()) break;

            eventIdsToRetry.forEach(event -> {
                if (!retry(event)) {
                    failedProcessed.incrementAndGet();
                }
            });

            totalProcessed += eventIdsToRetry.size();

            if (eventIdsToRetry.size() < SIZE) break;
        }

        log.info("[ProductOutboxScheduler] {} Outbox Events retried. Success: {}, Failed: {}",
                totalProcessed, totalProcessed - failedProcessed.get(), failedProcessed.get());
    }

    private boolean retry(ProductOutboxEvent event) {
        String eventId = event.getEventId();
        String eventType = event.getEventType();

        try {
            Function<ProductOutboxEvent, CompletableFuture<?>> publisher = publisherMap.get(eventType);

            if (publisher == null) {
                log.error("[ProductOutboxScheduler] No publisher for event type {}", eventType);
                return false;
            }

            productOutboxFacade.publish(eventId, publisher);
            return true;
        } catch(Exception e) {
            log.error("[ProductOutboxScheduler] {} Event retry failed: {}", eventId, e.getMessage(), e);
            return false;
        }
    }
}
