package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductCreateRequestDto(
        @NotNull(message = "Product info cannot be null")
        @Valid
        ProductInfoCreateRequestDto productInfo,

        @NotEmpty(message = "Product variants cannot be empty")
        @Valid
        List<ProductVariantCreateRequestDto> variants
) {
}