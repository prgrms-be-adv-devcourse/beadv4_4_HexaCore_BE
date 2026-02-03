package com.back.user.adapter.out;

import com.back.common.user.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreateKafkaPublisher {

    @Value("${custom.kafka.topic.user-created}")
    private String userCreatedTopic;

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(UserCreatedEvent event) {
        kafkaTemplate.send(userCreatedTopic, String.valueOf(event.id()), event)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("UserCreated 이벤트 전송 실패 userId={}", event.id(), ex);
                    else log.info("UserCreated 이벤트 전송 성공 userId={}", event.id());
                });
    }
}
