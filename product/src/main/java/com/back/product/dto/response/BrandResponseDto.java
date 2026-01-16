package com.back.product.dto.response;

import com.back.product.dto.BrandDto;
import lombok.Builder;

@Builder
public record BrandResponseDto(
        BrandDto brand
) {
}
