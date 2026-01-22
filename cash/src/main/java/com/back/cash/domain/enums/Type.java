package com.back.cash.domain.enums;

public enum Type {
    TOPUP_PG,    // PG 충전
    HOLD,    // 금액 홀딩
    RELEASE_ON_FAIL,    // 결제 실패로 인한 선홀딩 반환
    REFUND_ON_CANCEL, // 주문 취소로 전액 환불
    SETTLEMENT_PRINCIPAL,    // 정산
    SETTLEMENT_FEE     // 정산 수수료
}
