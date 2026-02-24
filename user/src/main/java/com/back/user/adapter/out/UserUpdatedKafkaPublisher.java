package com.back.user.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.user.app.event.UserUpdatedPayload;
import com.back.user.domain.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserUpdatedKafkaPublisher {

    @Value("${custom.kafka.topic.user-account-updated}")
    private String userUpdatedTopic;

    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishUserUpdated(UserUpdatedEvent event) {
        Envelope<UserUpdatedPayload> envelope = Envelope.of(userUpdatedTopic, UserUpdatedPayload.builder()
                .id(event.id())
                .address(event.address())
                .name(event.name())
                .phone(event.phone())
                .email(event.email())
                .nickname(event.nickname())
                .build());

        kafkaEventPublisher.publish(userUpdatedTopic, envelope);

        log.info("[ACCOUNT_UPDATED_KAFKA_PUBLISH] 회원 정보 수정 이벤트 발행 eventId={}, occurredAt={}, topic={}, userId={}",
                envelope.header().eventId(), envelope.header().occurrenceAt(), userUpdatedTopic, event.id());
    }

}
