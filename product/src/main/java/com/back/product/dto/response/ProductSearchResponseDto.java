package com.back.product.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductSearchResponseDto(
        Long productInfoId,
        String productName,
        String thumbnailUrl,
        String brandName,
        String categoryName,
        BigDecimal releasePrice
) {
}
