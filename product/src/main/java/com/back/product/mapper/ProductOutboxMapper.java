package com.back.product.mapper;

import com.back.product.domain.ProductOutboxEvent;
import org.springframework.stereotype.Component;

@Component
public class ProductOutboxMapper {
    private final String AGGREGATE_TYPE = "product";

    public ProductOutboxEvent toOutboxEvent(String eventId, String aggregateId, String eventType, String payload) {
        return ProductOutboxEvent.builder()
                .eventId(eventId)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();
    }
}
