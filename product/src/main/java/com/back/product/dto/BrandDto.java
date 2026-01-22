package com.back.product.dto;

import lombok.Builder;

@Builder
public record BrandDto(
        Long brandId,
        String name,
        String logoUrl
) {
}
