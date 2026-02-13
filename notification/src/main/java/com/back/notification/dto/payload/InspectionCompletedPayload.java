package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record InspectionCompletedPayload(
        @NotNull(message = "판매자 ID는 필수입니다.")
        Long sellerId,

        @NotNull(message = "검수 신청 일자는 필수입니다.")
        LocalDateTime requestedAt,

        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotNull(message = "가격은 필수입니다.")
        BigDecimal price,

        @NotBlank(message = "상품 사이즈는 필수입니다.")
        String productSize,

        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @NotBlank(message = "상품 번호는 필수입니다.")
        String productNumber
) implements KafkaPayload {
}
