package com.back.cash.domain.enums;

public enum Type {
    TOPUP_PG,    // PG 충전
    HOLD_ORDER,    // 금액 홀딩
    RELEASE_ORDER,    // 주문 취소
    SETTLEMENT_PRINCIPAL,    // 정산
    SETTLEMENT_FEE     // 정산 수수료
}
