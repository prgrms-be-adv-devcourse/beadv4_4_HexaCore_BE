package com.back.cash.dto.response;

import com.back.cash.dto.request.PaymentCompletedRequestDto;
import com.back.cash.dto.request.PaymentFailedRequestDto;

public record ConfirmResultResponseDto(
        boolean isSuccess,
        PaymentCompletedRequestDto completedDto,
        PaymentFailedRequestDto failedDto
) {
    public static ConfirmResultResponseDto success(PaymentCompletedRequestDto dto) {
        return new ConfirmResultResponseDto(true, dto, null);
    }
    public static ConfirmResultResponseDto fail(PaymentFailedRequestDto dto) {
        return new ConfirmResultResponseDto(false, null, dto);
    }
}
