package com.back.market.event;

import com.back.common.event.EventName;
import com.back.market.domain.Bidding;

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
    public static SellBiddingCreatedEvent of(Bidding bidding) {
        return new SellBiddingCreatedEvent(
                bidding.getId(),
                bidding.getMarketUser().getId(),
                bidding.getMarketProduct().getId(),
                bidding.getMarketProduct().getName(),
                bidding.getMarketProduct().getProductNumber(),
                bidding.getMarketProduct().getProductOption(),
                bidding.getMarketProduct().getBrandName(),
                bidding.getMarketProduct().getCategoryName(),
                bidding.getMarketProduct().getThumbnailImage(),
                bidding.getPrice()
        );
    }
}
