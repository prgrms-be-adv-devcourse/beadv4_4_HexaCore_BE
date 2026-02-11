package com.back.cash.dto.response;

import java.math.BigDecimal;

/**
 * 프론트엔드 전용 토스 결제 확인 성공 응답
 */
public record TossConfirmResponseDto(
        BigDecimal totalAmount
) {
    public static TossConfirmResponseDto from(ConfirmResultResponseDto result) {
        return new TossConfirmResponseDto(result.completedDto().totalAmount());
    }
}
