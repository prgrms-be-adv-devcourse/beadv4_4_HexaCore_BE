package com.back.settlement.domain;

import lombok.Getter;

@Getter
public enum SettlementItemStatus {
    INCLUDED,  // 포함됨: 정산에 포함된 정상 거래
    CANCELED,  // 취소됨: 주문 취소로 정산 제외
    REFUNDED,  // 환불됨: 환불 처리되어 정산 제외
    NEGATIVE   // 마이너스: 환불로 인한 차감 항목
}
