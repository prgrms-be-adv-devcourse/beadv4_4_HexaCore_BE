package com.back.product.dto.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductSearchDto(
        Long productInfoId,
        String productName,
        String thumbnailUrl,
        String brandName,
        String categoryName,
        BigDecimal releasePrice
) {
}
