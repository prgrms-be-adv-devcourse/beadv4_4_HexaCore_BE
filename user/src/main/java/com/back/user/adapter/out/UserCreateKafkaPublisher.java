package com.back.user.adapter.out;

import com.back.common.user.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreateKafkaPublisher {
    public static final String TOPIC_USER_CREATED = "user-created";
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(UserCreatedEvent event) {
        kafkaTemplate.send(TOPIC_USER_CREATED, String.valueOf(event.userId()), event)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("user replica 생성 이벤트 전송 실패 userId={}", event.userId(), ex);
                    else log.info("user replica 생성 이벤트 전송 성공 userId={}", event.userId());
                });
    }
}
