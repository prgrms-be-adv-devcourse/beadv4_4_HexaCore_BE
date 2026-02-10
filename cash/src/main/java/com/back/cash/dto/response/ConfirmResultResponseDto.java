package com.back.cash.dto.response;


import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import com.back.common.dto.cash.request.PaymentFailedRequestDto;

public record ConfirmResultResponseDto(
        Status status,
        PaymentCompletedRequestDto completedDto,
        PaymentFailedRequestDto failedDto,
        String errorCode,
        String failReason
) {
    public enum Status { SUCCESS, FAIL, PENDING }

    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isPending() { return status == Status.PENDING; }

    public static ConfirmResultResponseDto success(PaymentCompletedRequestDto dto) {
        return new ConfirmResultResponseDto(Status.SUCCESS, dto, null, null, null);
    }
    public static ConfirmResultResponseDto fail(PaymentFailedRequestDto dto, String errorCode, String failReason) {
        return new ConfirmResultResponseDto(Status.FAIL, null, dto, errorCode, failReason);
    }
    public static ConfirmResultResponseDto pending() {
        return new ConfirmResultResponseDto(Status.PENDING, null, null, null, null);
    }
}
