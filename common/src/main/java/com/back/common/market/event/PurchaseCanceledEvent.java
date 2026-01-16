package com.back.common.market.event;

import java.math.BigDecimal;

public record PurchaseCanceledEvent(
        Long biddingId,
        Long buyerUserId,
        Long sellerUserId,

        String productId,
        String productName,
        String productSize,

        BigDecimal price,
        String biddingPosition
) {
}
