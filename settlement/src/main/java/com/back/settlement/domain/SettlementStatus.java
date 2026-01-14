package com.back.settlement.domain;

import lombok.Getter;

@Getter
public enum SettlementStatus {
    PENDING,      // 정산 대기: 정산 집계 전, 아직 처리 시작 안 함
    IN_PROGRESS,  // 정산 진행 중: 정산 집계/계산 진행 중
    HOLD,         // 보류: 관리자가 일시 중단 (문제 발생 등)
    COMPLETED,    // 정산 완료: 정산 완료, 지급됨
    FAILED        // 정산 실패: 정산 처리 실패
}
