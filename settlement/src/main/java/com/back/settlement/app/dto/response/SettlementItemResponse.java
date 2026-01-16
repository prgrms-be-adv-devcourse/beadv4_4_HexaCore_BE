package com.back.settlement.app.dto.response;

import com.back.settlement.domain.SettlementItemStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SettlementItemResponse(
        Long settlementItemId,
        Long orderId,
        Long productId,
        Long buyerId,
        Long sellerId,
        SettlementItemStatus status,
        BigDecimal salesAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        LocalDateTime transactionAt
) {
}
