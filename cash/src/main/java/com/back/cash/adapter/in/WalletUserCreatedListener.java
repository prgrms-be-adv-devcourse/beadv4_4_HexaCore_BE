package com.back.cash.adapter.in;

import com.back.cash.app.usecase.CreateWalletUseCase;
import com.back.common.user.event.WalletCreateRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletUserCreatedListener {

    private final CreateWalletUseCase createWalletUseCase;

    @KafkaListener(
            topics = "${custom.kafka.topic.wallet-create-requests}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    @Transactional
    public void on(WalletCreateRequestedEvent event) {
        createWalletUseCase.createWallet(event.userId());
    }
}
