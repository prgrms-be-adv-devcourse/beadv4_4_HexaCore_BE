package com.back.market.event.payload;

import com.back.common.event.KafkaPayload;

import java.math.BigDecimal;

public record SellBiddingCreatedPayload(
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
) implements KafkaPayload {
    public static SellBiddingCreatedPayload of(Long biddingId, Long sellerUserId, Long productId, String productName, String productNumber, String productOption, String brandName, String categoryName, String thumbnailImage, BigDecimal currentPrice) {
        return new SellBiddingCreatedPayload(biddingId, sellerUserId, productId, productName, productNumber, productOption, brandName, categoryName, thumbnailImage, currentPrice);
    }
}
