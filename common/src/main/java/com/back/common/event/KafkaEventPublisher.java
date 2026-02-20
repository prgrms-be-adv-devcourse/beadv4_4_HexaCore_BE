package com.back.common.event;

import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(EventName event) {
        kafkaTemplate.send(event.getEventName(), event);
    }

    public void publish(String topic, EventName event) {
        kafkaTemplate.send(topic, event);
    }
}
