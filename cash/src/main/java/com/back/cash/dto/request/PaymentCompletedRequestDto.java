package com.back.cash.dto.request;

import com.back.cash.domain.enums.RelType;

import java.math.BigDecimal;

public record PaymentCompletedRequestDto(
        RelType relType,
        Long relId,
        BigDecimal totalAmount
) {}
