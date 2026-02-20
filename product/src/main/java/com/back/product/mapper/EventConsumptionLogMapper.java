package com.back.product.mapper;

import com.back.product.domain.EventConsumptionLog;
import org.springframework.stereotype.Component;

@Component
public class EventConsumptionLogMapper {
    public EventConsumptionLog toEntity(String eventId, String eventType, String message) {
        return EventConsumptionLog.builder()
                .eventId(eventId)
                .eventType(eventType)
                .message(message)
                .build();
    }
}
