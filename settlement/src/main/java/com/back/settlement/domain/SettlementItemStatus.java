package com.back.settlement.domain;

import java.util.Set;

public enum SettlementItemStatus {
    COLLECTED, // 수집됨: 일일 배치에서 수집된 미처리 항목
    INCLUDED,  // 포함됨: 정산에 포함된 정상 거래
    CANCELED,  // 취소됨: 주문 취소로 정산 제외
    REFUNDED,  // 환불됨: 환불 처리되어 정산 제외
    NEGATIVE;  // 마이너스: 환불로 인한 차감 항목

    public Set<SettlementItemStatus> getAllowedTransitions() {
        return switch (this) {
            case COLLECTED -> Set.of(INCLUDED);
            case INCLUDED -> Set.of(CANCELED, REFUNDED, NEGATIVE);
            case CANCELED, REFUNDED, NEGATIVE -> Set.of();
        };
    }

    public boolean canTransitionTo(SettlementItemStatus targetStatus) {
        return getAllowedTransitions().contains(targetStatus);
    }
}
