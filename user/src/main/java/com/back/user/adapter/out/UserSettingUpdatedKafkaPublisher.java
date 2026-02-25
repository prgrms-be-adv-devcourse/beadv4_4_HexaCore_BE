package com.back.user.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.user.app.event.UserSettingUpdatedPayload;
import com.back.user.domain.event.UserSettingUpdatedEvent;
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
public class UserSettingUpdatedKafkaPublisher {

    @Value("${custom.kafka.topic.user-setting-updated}")
    private String userSettingUpdatedTopic;

    private final KafkaEventPublisher kafkaEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishUserSettingUpdated(UserSettingUpdatedEvent event) {
        log.info("[UserSettingUpdatedKafkaPublisher] 알림설정 변경 이벤트 수신 - userId: {}", event.userId());

        Envelope<UserSettingUpdatedPayload> envelope = Envelope.of(
                userSettingUpdatedTopic,
                new UserSettingUpdatedPayload(
                        event.userId(),
                        event.bidStatusEnabled(),
                        event.productStatusEnabled(),
                        event.priceEnabled(),
                        event.settlementEnabled()
                )
        );

        kafkaEventPublisher.publish(userSettingUpdatedTopic, envelope);
        log.info("[UserSettingUpdatedKafkaPublisher] 알림설정 변경 Kafka 이벤트 발행 완료 - userId: {}", event.userId());
    }
}
