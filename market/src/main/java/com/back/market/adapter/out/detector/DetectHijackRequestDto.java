package com.back.market.adapter.out.detector;

import java.math.BigDecimal;

public record DetectHijackRequestDto(
        Long userId,
        String userEmail,
        String ip,
        BigDecimal transactionAmount
) {}
