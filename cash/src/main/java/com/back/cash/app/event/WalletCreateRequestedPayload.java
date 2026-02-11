package com.back.cash.app.event;

import com.back.common.event.KafkaPayload;

public record WalletCreateRequestedPayload(
        Long userId
) implements KafkaPayload
{}
