package com.back.product.mapper;

import com.back.product.domain.EventConsumptionLog;
import com.back.product.dto.command.EventConsumptionCommand;
import org.springframework.stereotype.Component;

@Component
public class EventConsumptionLogMapper {
    public EventConsumptionLog toEntity(EventConsumptionCommand command) {
        return EventConsumptionLog.builder()
                .eventId(command.eventId())
                .eventType(command.eventType())
                .topic(command.topic())
                .partition(command.partition())
                .offset(command.offset())
                .message(command.message())
                .build();
    }
}
