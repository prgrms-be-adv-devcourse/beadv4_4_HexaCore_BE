package com.back.product.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BrandListResponseDto(
        List<BrandResponseDto> brands
) {
}
