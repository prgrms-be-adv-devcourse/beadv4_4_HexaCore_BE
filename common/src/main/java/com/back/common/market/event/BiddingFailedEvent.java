package com.back.common.market.event;

import java.math.BigDecimal;

public record BiddingFailedEvent(
        Long sellerUserId,

        Long productId,
        String productName,
        String productSize,

        BigDecimal price,
        String biddingPosition
) {
}
