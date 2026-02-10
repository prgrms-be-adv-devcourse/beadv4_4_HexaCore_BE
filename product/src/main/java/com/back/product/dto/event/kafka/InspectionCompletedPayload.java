package com.back.product.dto.event.kafka;

import com.back.common.event.KafkaPayload;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InspectionCompletedPayload(
        Long sellerId,

        LocalDateTime requestedAt,  // 상품 검수 신청 일자

        Long productId,
        Long price,
        String productSize,
        String productName,
        String productNumber
) implements KafkaPayload {
}
