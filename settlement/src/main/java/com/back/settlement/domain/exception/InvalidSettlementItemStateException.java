package com.back.settlement.domain.exception;

import com.back.settlement.domain.SettlementItemStatus;
import lombok.Getter;

@Getter
public class InvalidSettlementItemStateException extends RuntimeException {
    private final SettlementItemStatus currentStatus;
    private final SettlementItemStatus targetStatus;

    public InvalidSettlementItemStateException(SettlementItemStatus currentStatus, SettlementItemStatus targetStatus) {
        super(String.format("잘못된 정산 항목 상태 전이: %s → %s (허용된 전이: %s)", currentStatus, targetStatus, currentStatus.getAllowedTransitions()));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
}
