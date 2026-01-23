package com.back.common.market.event;

import com.back.common.event.EventName;

import java.math.BigDecimal;

public record SellBiddingCreatedEvent(
        Long productId,
        BigDecimal currentPrice
) implements EventName {
}
