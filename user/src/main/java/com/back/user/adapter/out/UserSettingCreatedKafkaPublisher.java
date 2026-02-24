package com.back.user.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.user.app.event.UserSettingCreatedPayload;
import com.back.user.domain.event.UserSettingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSettingCreatedKafkaPublisher {

    @Value("${custom.kafka.topic.user-setting-created}")
    private String userSettingCreatedTopic;

    private final KafkaEventPublisher kafkaEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishUserSettingCreated(UserSettingCreatedEvent event) {
        log.info("[UserSettingCreatedKafkaPublisher] 알림설정 생성 이벤트 수신 - userId: {}", event.userId());

        Envelope<UserSettingCreatedPayload> envelope = Envelope.of(
                userSettingCreatedTopic,
                new UserSettingCreatedPayload(
                        event.userId(),
                        event.bidStatusEnabled(),
                        event.productStatusEnabled(),
                        event.priceEnabled(),
                        event.settlementEnabled()
                )
        );

        kafkaEventPublisher.publish(userSettingCreatedTopic, envelope);
        log.info("[UserSettingCreatedKafkaPublisher] 알림설정 생성 Kafka 이벤트 발행 완료 - userId: {}", event.userId());
    }
}
