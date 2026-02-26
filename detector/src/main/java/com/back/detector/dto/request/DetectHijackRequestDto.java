package com.back.detector.dto.request;

import java.math.BigDecimal;

public record DetectHijackRequestDto(
        Long userId,
        String userEmail,
        String ip,
        BigDecimal transactionAmount
) {}
