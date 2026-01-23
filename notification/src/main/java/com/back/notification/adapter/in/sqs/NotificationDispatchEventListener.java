package com.back.notification.adapter.in.sqs;

import com.back.notification.adapter.out.sqs.NotificationEventPublisher;
import com.back.notification.dto.NotificationCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationDispatchEventListener {
    private final NotificationEventPublisher publisher;

    @EventListener
    public void handle(NotificationCreatedEvent event) {
        publisher.send(event);
    }
}
