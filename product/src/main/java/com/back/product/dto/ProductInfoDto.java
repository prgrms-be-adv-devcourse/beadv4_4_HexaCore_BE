package com.back.product.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductInfoDto(
        Long productInfoId,
        BrandDto brand,
        CategoryDto category,
        String name,
        String code,
        BigDecimal releasePrice,
        LocalDateTime releaseDate
) {
}
