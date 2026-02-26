package com.back.user.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.user.app.event.FcmTokenChangedEvent;
import com.back.user.app.event.FcmTokenChangedPayload;
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
public class FcmTokenChangedKafkaPublisher {

    @Value("${custom.kafka.topic.user-fcm-token-changed}")
    private String fcmTokenChangedTopic;

    private final KafkaEventPublisher kafkaEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishFcmTokenChanged(FcmTokenChangedEvent event) {
        log.info("[FcmTokenChangedKafkaPublisher] FCM 토큰 변경 이벤트 수신 - userId: {}", event.userId());

        Envelope<FcmTokenChangedPayload> envelope = Envelope.of(
                fcmTokenChangedTopic,
                new FcmTokenChangedPayload(
                        event.userId(),
                        event.fcmToken()
                )
        );

        kafkaEventPublisher.publish(fcmTokenChangedTopic, envelope);
        log.info("[FcmTokenChangedKafkaPublisher] FCM 토큰 변경 Kafka 이벤트 발행 완료 - userId: {}", event.userId());
    }
}
