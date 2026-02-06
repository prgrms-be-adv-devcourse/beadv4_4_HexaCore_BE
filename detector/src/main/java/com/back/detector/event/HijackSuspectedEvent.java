package com.back.detector.event;

import java.math.BigDecimal;

/**
 * 계정 탈취 의심 이벤트
 * 새 IP에서 거래 금액이 20만원을 초과할 때 발생
 * 이벤트 발생 시 사용자에게 이메일 알림 발송
 */
public record HijackSuspectedEvent(
        Long userId,
        String email,
        String existingIps,
        String currentIp,
        BigDecimal transactionAmount,
        String reason
) {
}
