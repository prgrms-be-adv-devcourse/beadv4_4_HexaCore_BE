package com.back.common.market.event;

import com.back.common.event.EventName;

import java.math.BigDecimal;

public record BiddingFailedEvent(
        Long sellerUserId,

        Long productId,
        String productName,
        String productSize,

        BigDecimal price,
        String biddingPosition
) implements EventName {
}
