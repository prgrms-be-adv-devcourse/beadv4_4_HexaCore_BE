package com.back.user.domain.event;

public record UserSettingCreatedEvent(
        Long userId,
        boolean bidStatusEnabled,
        boolean productStatusEnabled,
        boolean priceEnabled,
        boolean settlementEnabled
) {
}
