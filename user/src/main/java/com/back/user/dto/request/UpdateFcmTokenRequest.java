package com.back.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateFcmTokenRequest(
        @NotBlank String fcmToken
) {
}
