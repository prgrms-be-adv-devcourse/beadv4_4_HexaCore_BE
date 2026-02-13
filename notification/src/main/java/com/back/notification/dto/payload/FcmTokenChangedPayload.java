package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FcmTokenChangedPayload(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String fcmToken
) implements KafkaPayload {
}
