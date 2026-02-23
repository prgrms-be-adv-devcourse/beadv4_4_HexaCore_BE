package com.back.market.dto.request;

import java.math.BigDecimal;

public record MarketProductDto(
        String name,
        String productNumber,
        String thumbnailImage,
        BigDecimal releasePrice,
        String brandName,
        String categoryName
) {
}
