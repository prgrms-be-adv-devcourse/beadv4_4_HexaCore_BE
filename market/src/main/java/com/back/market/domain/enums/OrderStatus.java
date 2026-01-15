package com.back.market.domain.enums;

// 주문 상태를 관리하는 Enum
public enum OrderStatus {
    HOLD,       // 결제 대기 (낙찰 직후)
    PAID,       // 결제 완료 (거래 성사)
    CANCELLED   // 주문 취소
}
