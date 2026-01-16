package com.back.product.dto.response;

import lombok.Builder;

@Builder
public record BrandResponseDto(
        Long id,
        String name,
        String logoUrl
) {
}
