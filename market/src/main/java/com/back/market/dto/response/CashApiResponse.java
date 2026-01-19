package com.back.market.dto.response;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;

import lombok.Builder;

@Builder
public record CashApiResponse<T> (
        String code,
        String message,
        T data
) {

    // ... 편의 메서드(isSuccess) 그대로 ...
    public boolean isSuccess() {
        return SuccessCode.OK.getCode().equals(this.code);
    }
    
    public boolean isChargeFailed() {
        return FailureCode.WALLET_CHARGE_FAILED.getCode().equals(this.code);
    }
}
