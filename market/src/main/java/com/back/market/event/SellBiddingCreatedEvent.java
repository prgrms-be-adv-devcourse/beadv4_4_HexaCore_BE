package com.back.market.event;

import com.back.common.event.EventName;

import java.math.BigDecimal;

public record SellBiddingCreatedEvent(
        Long biddingId,
        Long sellerUserId,
        Long productId,
        String productName,
        String productNumber,
        String productOption,
        String brandName,
        String categoryName,
        String thumbnailImage,
        BigDecimal currentPrice
) implements EventName {
    public static SellBiddingCreatedEvent of(Long biddingId, Long sellerUserId, Long productId, String productName, String productNumber, String productOption, String brandName, String categoryName, String thumbnailImage, BigDecimal currentPrice) {
        return new SellBiddingCreatedEvent(biddingId, sellerUserId, productId, productName, productNumber, productOption, brandName, categoryName, thumbnailImage, currentPrice);
    }
}
