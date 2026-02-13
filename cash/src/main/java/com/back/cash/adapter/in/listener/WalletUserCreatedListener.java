package com.back.cash.adapter.in.listener;

import com.back.cash.app.event.WalletCreateRequestedPayload;
import com.back.cash.app.usecase.CreateWalletUseCase;
import com.back.common.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletUserCreatedListener {

    private final CreateWalletUseCase createWalletUseCase;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.user-wallet-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void on(String message) {
        Envelope<WalletCreateRequestedPayload> event;
        try {
            event = jsonMapper.readValue(message, new TypeReference<Envelope<WalletCreateRequestedPayload>>() {});
        } catch (Exception e) {
            log.error("[ERROR_WALLET_CREATED_CONSUME] 역직렬화 실패. message={}", message, e);
            return;
        }

        WalletCreateRequestedPayload data = event.payload();

        createWalletUseCase.createWallet(data.userId());

        log.info("[WALLET_CREATED_CONSUME] 지갑 생성 완료 - userId: {}", data.userId());
    }
}
