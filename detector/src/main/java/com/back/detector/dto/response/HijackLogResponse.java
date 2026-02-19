package com.back.detector.dto.response;

import com.back.detector.domain.HijackLog;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record HijackLogResponse(
        Long id,
        Long userId,
        String userEmail,
        String currentIp,
        String existingIps,
        BigDecimal transactionAmount,
        String reason,
        LocalDateTime createdAt
) {
    public static HijackLogResponse from(HijackLog log) {
        return HijackLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userEmail(log.getUserEmail())
                .currentIp(log.getCurrentIp())
                .existingIps(log.getExistingIps())
                .transactionAmount(log.getTransactionAmount())
                .reason(log.getReason())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
