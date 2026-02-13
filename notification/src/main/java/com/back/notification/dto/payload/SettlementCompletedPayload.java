package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record SettlementCompletedPayload(
        @NotNull(message = "판매자 ID는 필수입니다.")
        Long sellerId,

        @NotNull(message = "정산 시작일은 필수입니다.")
        LocalDateTime startAt,

        @NotNull(message = "정산 종료일은 필수입니다.")
        LocalDateTime endAt,

        @NotNull(message = "총 정산 금액은 필수입니다.")
        BigDecimal totalNetAmount
) implements KafkaPayload {
}
