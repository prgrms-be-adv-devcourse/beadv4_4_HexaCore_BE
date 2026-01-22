package com.back.settlement.domain.exception;

import com.back.settlement.domain.SettlementStatus;

public class InvalidSettlementStateException extends RuntimeException {
    private final SettlementStatus currentStatus;
    private final SettlementStatus targetStatus;

    public InvalidSettlementStateException(SettlementStatus currentStatus, SettlementStatus targetStatus) {
        super(String.format("잘못된 상태 전이: %s → %s (허용된 전이: %s)", currentStatus, targetStatus, currentStatus.getAllowedTransitions()));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidSettlementStateException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public SettlementStatus getCurrentStatus() {
        return currentStatus;
    }

    public SettlementStatus getTargetStatus() {
        return targetStatus;
    }
}
