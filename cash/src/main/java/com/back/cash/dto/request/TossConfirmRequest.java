package com.back.cash.dto.request;

import java.math.BigDecimal;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,    // = Payment.tossOrderId
        BigDecimal amount  // = Payment.pgAmount
) {}
