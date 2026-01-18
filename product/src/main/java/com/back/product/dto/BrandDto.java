package com.back.product.dto;

import lombok.Builder;

@Builder
public record BrandDto(
        String name,
        String logoUrl
) {
}
