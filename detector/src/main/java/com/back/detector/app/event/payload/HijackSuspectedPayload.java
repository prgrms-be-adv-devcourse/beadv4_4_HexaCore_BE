package com.back.detector.app.event.payload;

import com.back.common.event.KafkaPayload;

import java.math.BigDecimal;

public record HijackSuspectedPayload(
        Long userId,
        String email,
        String existingIps,
        String currentIp,
        BigDecimal transactionAmount,
        String reason
) implements KafkaPayload {
}
