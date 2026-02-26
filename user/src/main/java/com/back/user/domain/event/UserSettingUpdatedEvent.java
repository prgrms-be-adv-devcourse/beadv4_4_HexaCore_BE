package com.back.user.domain.event;

public record UserSettingUpdatedEvent(
        Long userId,
        boolean bidStatusEnabled,
        boolean productStatusEnabled,
        boolean priceEnabled,
        boolean settlementEnabled
) {
}
