package com.back.user.dto.request;

public record UpdateNotificationSettingsRequest(
        Boolean bidStatusEnabled,
        Boolean productStatusEnabled,
        Boolean priceEnabled,
        Boolean settlementEnabled
) {
}
