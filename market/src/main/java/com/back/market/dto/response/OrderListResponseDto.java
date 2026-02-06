package com.back.market.dto.response;

import com.back.market.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderListResponseDto(
        Long orderId,
        String productName,
        String productImageUrl,
        String size,
        BigDecimal price,
        OrderStatus orderStatus,
        LocalDateTime paymentDate //DB의 paymentDate 컬럼
) {

}
