package com.back.common.market.event;

import com.back.common.event.EventName;

import java.math.BigDecimal;

public record SellBiddingCreatedEvent(
        Long sellerUserId,
        Long productId,
        String productName,
        String productNumber,
        String brandName,
        String categoryName,
        String productOption,
        String thumbnailImage,
        BigDecimal currentPrice
) implements EventName {
}
