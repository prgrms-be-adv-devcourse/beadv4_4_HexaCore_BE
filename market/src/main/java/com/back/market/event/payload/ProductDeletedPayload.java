package com.back.market.event.payload;

import com.back.common.event.KafkaPayload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProductDeletedPayload(
        @NotNull(message = "상품 정보 ID는 필수입니다.")
        @Min(value = 1, message = "상품 정보 ID는 1 이상의 정수여야 합니다.")
        Long productInfoId
) implements KafkaPayload {
}
