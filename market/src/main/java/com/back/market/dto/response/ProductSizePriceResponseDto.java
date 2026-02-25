package com.back.market.dto.response;

import java.math.BigDecimal;

public record ProductSizePriceResponseDto(
        Long productId,
        String productOption,
        BigDecimal instantBuyPrice,
        BigDecimal instantSellPrice
) {
}
