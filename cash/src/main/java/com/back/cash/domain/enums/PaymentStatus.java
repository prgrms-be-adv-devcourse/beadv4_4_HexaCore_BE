package com.back.cash.domain.enums;

public enum PaymentStatus {
    READY,
    TOSS_CONFIRMED, // 토스 confirm 성공, DB 반영 대기 중
    DONE,
    CANCELED,
    FAIL
}
