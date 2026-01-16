package com.back.common.product.event;

import java.time.LocalDateTime;

public record InspectionCompletedEvent(
        Long sellerId,

        LocalDateTime requestedAt,  // 상품 검수 신청 일자

        Long productId,
        Long price,
        String productSize,
        String productName,
        String productNumber
) {
}
