package com.back.settlement.domain;

import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SettlementStatus {
    PENDING("정산 대기"),      // 정산 대기: 정산 시작 전
    IN_PROGRESS("정산 진행중"),  // 정산 진행 중: 캐시 지급 요청
    HOLD("보류"),         // 보류: 관리자가 일시 중단 (문제 발생 등)
    COMPLETED("정산 완료"),    // 정산 완료: 정산 완료, 캐시 지급됨
    FAILED("정산 실패");        // 정산 실패: 정산 처리 실패, 캐시 지급 안 됨

    private final String value;

    public Set<SettlementStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> Set.of(IN_PROGRESS, HOLD, FAILED);
            case IN_PROGRESS -> Set.of(COMPLETED, HOLD, FAILED);
            case HOLD -> Set.of(IN_PROGRESS, FAILED);
            case COMPLETED, FAILED -> Set.of();  // 최종 상태, 전이 불가
        };
    }

    public boolean canTransitionTo(SettlementStatus targetStatus) {
        return getAllowedTransitions().contains(targetStatus);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean isProcessing() {
        return !isTerminal();
    }
}
