package com.back.market.event.payload;

import com.back.common.event.KafkaPayload;

import java.math.BigDecimal;

public record OrderCreatedPayload (
        Long orderId,
        Long buyerUserId,
        Long sellerUserId,
        Long productId,
        String productName,
        String productSize,
        String thumbnailImage,
        String brandName,
        BigDecimal price,
        String biddingPosition  // 판매입찰 / 구매입찰
) implements KafkaPayload {
    public static OrderCreatedPayload of(Long orderId, Long buyerUserId, Long sellerUserId, Long productId, String productName, String productSize, String thumbnailImage, String brandName, BigDecimal price, String biddingPosition) {
        return new OrderCreatedPayload(orderId, buyerUserId, sellerUserId, productId, productName, productSize, thumbnailImage, brandName, price, biddingPosition);
    }
}
