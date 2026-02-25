package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;

public record UserSettingUpdatedPayload(
        Long userId,
        boolean bidStatusEnabled,
        boolean productStatusEnabled,
        boolean priceEnabled,
        boolean settlementEnabled
) implements KafkaPayload {
}
