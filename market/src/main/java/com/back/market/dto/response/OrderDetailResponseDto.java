package com.back.market.dto.response;

import com.back.market.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailResponseDto(
        Long orderId,
        String productNumber,
        String productName,
        String productSize,
        String brandName,
        String thumbnailImage,
        BigDecimal price,
        OrderStatus orderStatus,
        String address,
        LocalDateTime paymentDate //결제 완료일(paymentDate)
) {
}
