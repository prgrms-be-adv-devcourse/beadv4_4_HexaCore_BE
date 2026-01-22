package com.back.cash.dto.request;

public record TossFailRequestDto(
        String orderId,    // = Payment.tossOrderId
        String code,      // toss fail code
        String message    // toss fail message
) {}
