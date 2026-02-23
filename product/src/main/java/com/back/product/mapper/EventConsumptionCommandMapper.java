package com.back.product.mapper;

import com.back.product.dto.command.EventConsumptionCommand;
import org.springframework.stereotype.Component;

@Component
public class EventConsumptionCommandMapper {
    public EventConsumptionCommand toCommand(String eventId, String eventType, String topic, Integer partition, Long offset, String message) {
        return EventConsumptionCommand.builder()
                .eventId(eventId)
                .eventType(eventType)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .message(message)
                .build();
    }
}
