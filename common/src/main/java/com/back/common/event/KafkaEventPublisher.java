package com.back.common.event;

import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, EventName> kafkaTemplate;

    public void publish(EventName event) {
        kafkaTemplate.send(event.getEventName(), event);
    }
}
