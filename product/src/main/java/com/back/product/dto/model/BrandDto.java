package com.back.product.dto.model;

import lombok.Builder;

@Builder
public record BrandDto(
        Long brandId,
        String name,
        String imageUrl
) {
}
