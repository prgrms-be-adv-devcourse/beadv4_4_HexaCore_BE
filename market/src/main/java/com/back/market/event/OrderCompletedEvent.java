package com.back.market.event;

import com.back.common.event.EventName;
import com.back.market.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCompletedEvent (
        Long orderId,
        Long productId,
        Long buyerId,
        Long sellerId,
        String sellerName, // 판매자 이름
        BigDecimal price,
        OrderStatus orderStatus,
        LocalDateTime confirmedAt
) implements EventName {
    public static OrderCompletedEvent of(Long orderId, Long productId, Long buyerId, Long sellerId, String sellerName, BigDecimal price, OrderStatus orderStatus,LocalDateTime confirmedAt) {
        return new OrderCompletedEvent(orderId, productId, buyerId, sellerId, sellerName, price, orderStatus, confirmedAt);
    }
}
