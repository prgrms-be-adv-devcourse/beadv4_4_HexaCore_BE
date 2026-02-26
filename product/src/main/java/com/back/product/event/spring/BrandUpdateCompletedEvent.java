package com.back.product.event.spring;

import com.back.product.dto.model.BrandDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BrandUpdateCompletedEvent(
        @NotEmpty(message = "이벤트 ID가 필요합니다.")
        String eventId,

        @NotNull(message = "BrandDto cannot be null")
        @Valid
        BrandDto brand
) {
}
