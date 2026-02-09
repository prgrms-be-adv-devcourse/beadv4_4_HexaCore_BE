package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record BrandListCreateRequestDto(
        @NotEmpty(message = "Brands cannot be null")
        @Valid
        List<BrandDataRequestDto> brands
) {
}
