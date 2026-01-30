package com.back.user.dto.response;

public record NotificationSettingResponse(
        boolean bidStatusEnabled,
        boolean productStatusEnabled,
        boolean priceEnabled,
        boolean settlementEnabled
) {
}
