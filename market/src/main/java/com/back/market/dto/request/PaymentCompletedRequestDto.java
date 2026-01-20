package com.back.market.dto.request;

import com.back.market.dto.enums.RelType;

/**
 * 캐시 -> 마켓 : 결제 성공 콜백(내부 통지)
 * @param relType
 * @param relId
 */
public record PaymentCompletedRequestDto(
        RelType relType,
        Long relId
) {
    public static PaymentCompletedRequestDto of(RelType relType, Long relId) {
        return new PaymentCompletedRequestDto(relType, relId);
    }
}
