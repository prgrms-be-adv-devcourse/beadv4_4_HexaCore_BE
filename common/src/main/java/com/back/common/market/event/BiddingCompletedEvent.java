package com.back.common.market.event;

import com.back.common.event.EventName;

import java.math.BigDecimal;

public record BiddingCompletedEvent(
        Long biddingId,
        Long buyerUserId,
        Long sellerUserId,

        Long productId,
        String productName,
        String productSize,
        String thumbnailImage,
        String brandName,

        BigDecimal price,
        String biddingPosition  // 판매입찰 / 구매입찰
) implements EventName {
}
