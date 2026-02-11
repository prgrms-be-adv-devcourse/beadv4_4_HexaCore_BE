package com.back.user.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.user.app.event.UserCreatedPayload;
import com.back.user.domain.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreateKafkaPublisher {

    @Value("${custom.kafka.topic.user-account-created}")
    private String userCreatedTopic;

    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(UserCreatedEvent event) {
        Envelope<UserCreatedPayload> envelope = Envelope.of(userCreatedTopic, new UserCreatedPayload(
                event.id(),
                event.nickname(),
                event.name(),
                event.email(),
                event.address(),
                event.phone(),
                event.profileImageUrl(),
                event.ipAddress()
        ));

        kafkaEventPublisher.publish(userCreatedTopic, envelope);

        log.info("[USER_CREATED_KAFKA_PUBLISH] 회원 복제 이벤트 생성 eventId={}, occurredAt={}, topic={}, userId={}",
                envelope.header().eventId(), envelope.header().occurrenceAt(), userCreatedTopic, event.id());
    }
}
