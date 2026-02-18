package com.back.product.event.spring;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BrandDeletionCompletedEvent(
        @NotNull(message = "브랜드 ID는 필수입니다.")
        @Min(value = 1, message = "브랜드 ID는 1 이상의 정수여야 합니다.")
        Long brandId
) {
}
