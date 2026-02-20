package com.back.market.event;

import com.back.common.event.EventName;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingPosition;

import java.math.BigDecimal;

public record OrderCreatedEvent(
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
) implements EventName {
    public static OrderCreatedEvent of(Order order, BiddingPosition position) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getBuyBidding().getMarketUser().getId(),
                order.getSellBidding().getMarketUser().getId(),
                order.getBuyBidding().getMarketProduct().getId(),
                order.getBuyBidding().getMarketProduct().getName(),
                order.getBuyBidding().getMarketProduct().getProductOption(),
                order.getBuyBidding().getMarketProduct().getThumbnailImage(),
                order.getBuyBidding().getMarketProduct().getBrandName(),
                order.getPrice(),
                position.name()
        );
    }
}
