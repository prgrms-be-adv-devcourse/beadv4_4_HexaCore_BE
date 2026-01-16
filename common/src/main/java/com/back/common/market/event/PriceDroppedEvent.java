package com.back.common.market.event;

import java.math.BigDecimal;

public record PriceDroppedEvent(
        Long userId,

        Long productId,
        String productName,
        String productSize,
        String thumbnailImage,
        String brandName,

        BigDecimal targetPrice
) {
}
