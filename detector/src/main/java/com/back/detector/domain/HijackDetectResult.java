package com.back.detector.app;

import java.math.BigDecimal;

public record HijackDetectResult(
        Long userId,
        String userEmail,
        String currentIp,
        String existingIps,
        BigDecimal transactionAmount,
        String reason
) {}
