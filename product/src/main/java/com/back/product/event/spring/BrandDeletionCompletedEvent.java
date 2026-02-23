package com.back.product.event.spring;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BrandDeletionCompletedEvent(
        @NotEmpty(message = "이벤트 ID는 필수입니다.")
        String eventId,

        @NotNull(message = "브랜드 ID는 필수입니다.")
        @Min(value = 1, message = "브랜드 ID는 1 이상의 정수여야 합니다.")
        Long brandId
) {
}
