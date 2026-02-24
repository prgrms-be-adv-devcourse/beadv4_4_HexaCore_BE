package com.back.cash.domain.outbox.enums;

public enum OutboxStatus {
    PENDING,      // 대기: 생성됨, 발송 전
    SENT,         // 성공: 발송 완료
    FAILED        // 실패: 발송 실패
}
