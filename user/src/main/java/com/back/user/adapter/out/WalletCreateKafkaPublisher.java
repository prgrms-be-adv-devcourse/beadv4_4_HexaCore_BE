package com.back.user.adapter.out;

import com.back.common.user.event.WalletCreateRequestedEvent;
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
public class WalletCreateKafkaPublisher {

    @Value("${custom.kafka.topic.wallet-create-requests}")
    private String walletCreateRequestsTopic;

    private final KafkaTemplate<String, WalletCreateRequestedEvent> kafkaTemplate;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishWalletCreate(WalletCreateRequestedEvent event) {
        kafkaTemplate.send(walletCreateRequestsTopic, String.valueOf(event.userId()), event)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("wallet 생성 이벤트 전송 실패 userId={}", event.userId(), ex);
                    else log.info("wallet 생성 이벤트 전송 성공 userId={}", event.userId());
                });
    }

}
