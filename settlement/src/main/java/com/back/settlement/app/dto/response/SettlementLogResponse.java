package com.back.settlement.app.dto.response;

import com.back.settlement.domain.SettlementLog.ActorType;
import com.back.settlement.domain.SettlementStatus;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SettlementLogResponse(
        Long logId,
        Long settlementId,
        SettlementStatus previousStatus,
        SettlementStatus newStatus,
        String reason,
        ActorType actorType,
        Long actorId,
        LocalDateTime createdAt
) {
}
