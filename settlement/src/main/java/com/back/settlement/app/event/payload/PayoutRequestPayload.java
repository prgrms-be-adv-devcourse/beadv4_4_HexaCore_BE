package com.back.settlement.app.event.payload;

import com.back.common.event.KafkaPayload;
import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayoutRequestPayload(
        Long settlementId,
        Long payeeId,
        BigDecimal totalGrossAmount,
        BigDecimal totalFeeAmount,
        BigDecimal totalNetAmount,
        LocalDateTime occurredAt
) implements KafkaPayload {

    public static final String EVENT_TYPE = "settlement.payout.requested";

    public static PayoutRequestPayload from(SettlementInternalCompletedEvent event) {
        return new PayoutRequestPayload(
                event.settlementId(),
                event.sellerId(),
                event.totalSalesAmount(),
                event.totalFeeAmount(),
                event.totalNetAmount(),
                event.occurredAt()
        );
    }
}