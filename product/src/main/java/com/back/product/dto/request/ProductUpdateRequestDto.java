package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductUpdateRequestDto(
        @NotNull(message = "Product info cannot be null")
        @Valid
        ProductInfoUpdateRequestDto productInfo,

        @NotEmpty(message = "Product variants cannot be empty")
        @Valid
        List<ProductVariantUpdateRequestDto> variants
) {
}