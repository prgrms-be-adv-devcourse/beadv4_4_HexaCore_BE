package com.back.common.market.event;

import com.back.common.event.EventName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCompletedEvent (
        Long orderId,
        Long productId,
        Long buyerId,
        Long sellerId,
        String sellerName, // 판매자 이름(닉네임)
        BigDecimal price,
        LocalDateTime confirmedAt
) implements EventName {
}
