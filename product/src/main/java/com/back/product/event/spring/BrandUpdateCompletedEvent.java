package com.back.product.event.spring;

import com.back.product.dto.model.BrandDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BrandUpdateCompletedEvent(
        @NotNull(message = "BrandDto cannot be null")
        @Valid
        BrandDto brand
) {
}
