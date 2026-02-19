package com.back.market.event.payload;

import com.back.common.event.KafkaPayload;
import com.back.market.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCompletedPayload(
        Long orderId,
        Long productId,
        Long buyerId,
        Long sellerId,
        String sellerName, // 판매자 이름
        BigDecimal price,
        OrderStatus orderStatus,
        LocalDateTime confirmedAt
) implements KafkaPayload {
    public static OrderCompletedPayload of(Long orderId, Long productId, Long buyerId, Long sellerId, String sellerName, BigDecimal price, OrderStatus orderStatus, LocalDateTime confirmedAt) {
        return new OrderCompletedPayload(orderId, productId, buyerId, sellerId, sellerName, price, orderStatus, confirmedAt);
    }
}