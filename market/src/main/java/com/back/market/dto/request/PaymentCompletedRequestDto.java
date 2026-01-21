package com.back.market.dto.request;

import com.back.market.dto.enums.RelType;

import java.math.BigDecimal;

/**
 * 캐시 -> 마켓 : 결제 성공 콜백(내부 통지)
 * @param relType
 * @param relId
 * @param totalAmount 총 결제 금액(사용자 지갑에 존재하던 예치금 + PG사 결제 금액)
 */
public record PaymentCompletedRequestDto(
        RelType relType,
        Long relId,
        // Market 모듈에서 totalAmount와 Order 테이블의 금액을 검증하기 위해 필요
        BigDecimal totalAmount
) {
    public static PaymentCompletedRequestDto of(RelType relType, Long relId, BigDecimal totalAmount) {
        return new PaymentCompletedRequestDto(relType, relId, totalAmount);
    }
}
