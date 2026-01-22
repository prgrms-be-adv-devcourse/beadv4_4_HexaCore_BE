package com.back.common.dto.settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Market 모듈 → Settlement 모듈로 전달되는 데이터
public record SettlementTargetOrder(
        Long orderId,
        Long productId,
        Long buyerId,
        Long sellerId,
        String sellerName,
        BigDecimal price,
        LocalDateTime confirmedAt
) {
}
