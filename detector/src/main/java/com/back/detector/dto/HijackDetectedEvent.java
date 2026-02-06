package com.back.detector.dto;

import java.math.BigDecimal;

/**
 * 계정 탈취 감지 도메인 이벤트
 */
public record HijackDetectedEvent(
        Long userId,
        String email,
        String existingIps,
        String currentIp,
        BigDecimal transactionAmount,
        String reason
) {
}
