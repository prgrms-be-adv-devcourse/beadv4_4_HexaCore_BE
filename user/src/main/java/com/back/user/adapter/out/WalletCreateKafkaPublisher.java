package com.back.user.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.user.app.event.WalletCreateRequestedPayload;
import com.back.user.domain.event.WalletCreateRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletCreateKafkaPublisher {

    @Value("${custom.kafka.topic.user-wallet-created}")
    private String walletCreateRequestsTopic;

    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishWalletCreate(WalletCreateRequestedEvent event) {
        Envelope<WalletCreateRequestedPayload> envelope = Envelope.of("user.wallet.created", new WalletCreateRequestedPayload(event.userId()));

        kafkaEventPublisher.publish(walletCreateRequestsTopic, envelope);

        log.info("[WALLET_CREATED_KAFKA_PUBLISH] eventId={}, occurredAt={}, topic={}, userId={}",
                envelope.header().eventId(), envelope.header().occurrenceAt(), walletCreateRequestsTopic, event.userId());
    }

}
