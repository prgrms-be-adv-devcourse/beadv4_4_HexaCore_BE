package com.back.cash.dto.response;

import java.math.BigDecimal;

/**
 * 프론트엔드 전용 토스 결제 확인 응답
 */
public record TossConfirmResponseDto(
        String status,
        BigDecimal totalAmount,
        String failReason
) {
    public static TossConfirmResponseDto from(ConfirmResultResponseDto result) {
        if (result.isSuccess()) {
            return new TossConfirmResponseDto("SUCCESS", result.completedDto().totalAmount(), null);
        }
        return new TossConfirmResponseDto("FAIL", null, result.failReason());
    }
}
