package com.back.user.app.event;

import com.back.common.event.KafkaPayload;

public record UserSettingUpdatedPayload(
        Long userId,
        boolean bidStatusEnabled,
        boolean productStatusEnabled,
        boolean priceEnabled,
        boolean settlementEnabled
) implements KafkaPayload {
}
