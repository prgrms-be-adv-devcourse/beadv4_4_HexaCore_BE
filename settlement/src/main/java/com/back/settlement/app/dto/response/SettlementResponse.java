package com.back.settlement.app.dto.response;

import com.back.settlement.domain.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SettlementResponse(
        Long settlementId,
        Long sellerId,
        SettlementStatus status,
        LocalDateTime expectedAt,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime completedAt,
        BigDecimal totalSalesAmount,
        BigDecimal totalFeeAmount,
        BigDecimal totalNetAmount
) {
}