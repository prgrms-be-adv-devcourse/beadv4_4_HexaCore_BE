package com.back.market.domain.enums;

// 주문 상태를 관리하는 Enum
public enum OrderStatus {
    HOLD,        // 결제 대기 (낙찰 직후)
    PAID,        // 결제 완료 (거래 성사)
    CANCELLED,   // 주문 취소(결제 후, 배송 전까지만 가능)
    CANCELLED_PAYMENT_FAILED,
    DELIVERY_PROCESSING,    // 배송중
    DELIVERY_COMPLETED, // 배송 완료(환불 요청은 배송 완료일로부터 일주일까지 가능)
    REFUNDED, // 주문 환불(배송중~배송완료 후 1주까지는 환불가능)
    COMPLETED    // 구매 확정(정산 가능)
}
