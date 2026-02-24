package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;

public record UserSettingCreatedPayload(
        Long userId,
        boolean bidStatusEnabled,
        boolean productStatusEnabled,
        boolean priceEnabled,
        boolean settlementEnabled
) implements KafkaPayload {
}
