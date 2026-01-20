package com.back.market.dto.request;

import com.back.market.dto.enums.RelType;

/**
 * 캐시 -> 마켓 : 결제 실패 콜백(내부 통지)
 * @param relType
 * @param relId
 */
public record PaymentFailedRequestDto(
        RelType relType,
        Long relId
) {
    public static PaymentFailedRequestDto of(RelType relType, Long relId) {
        return new PaymentFailedRequestDto(relType, relId);
    }
}
