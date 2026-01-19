package com.back.market.dto.response;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashApiResponse<T> {
    private String code;
    private String message;
    private T data;

    // ... 편의 메서드(isSuccess) 그대로 ...
    public boolean isSuccess() {
        return SuccessCode.OK.getCode().equals(this.code);
    }
    public boolean isChargeFailed() {
        return FailureCode.WALLET_CHARGE_FAILED.getCode().equals(this.code);
    }
}
