package com.back.product.dto.response;

import com.back.product.dto.BrandDto;
import lombok.Builder;

import java.util.List;

@Builder
public record BrandListResponseDto(
        List<BrandDto> brands
) {
}
