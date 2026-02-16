package com.back.product.event.kafka;

import com.back.common.event.KafkaPayload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InspectionCompletedPayload(
        @NotNull(message = "판매자 ID는 필수입니다.")
        @Min(value = 1, message = "판매자 ID는 1 이상의 정수여야 합니다.")
        Long sellerId,

        @NotNull(message = "상품 검수 신청 일자는 필수입니다.")
        LocalDateTime requestedAt,  // 상품 검수 신청 일자

        @NotNull(message = "상품 ID는 필수입니다.")
        @Min(value = 1, message = "상품 ID는 1 이상의 정수여야 합니다.")
        Long productId,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0 이상의 정수여야 합니다.")
        Long price,

        @NotBlank(message = "상품 사이즈는 필수입니다.")
        @Size(min = 1, max = 50, message = "상품 사이즈는 1자 이상 50자 이하여야 합니다.")
        String productSize,

        @NotBlank(message = "상품 이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "상품 이름은 1자 이상 100자 이하여야 합니다.")
        String productName,

        @NotBlank(message = "상품 번호는 필수입니다.")
        @Size(min = 1, max = 50, message = "상품 번호는 1자 이상 50자 이하여야 합니다.")
        String productNumber
) implements KafkaPayload {
}
