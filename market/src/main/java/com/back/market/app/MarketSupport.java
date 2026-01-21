package com.back.market.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.client.CashClient;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.response.CashApiResponse;
import com.back.market.dto.response.PayAndHoldResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarketSupport {
    private final CashClient cashClient;

    public PayAndHoldResponseDto getPayAndHoldResult(PayAndHoldRequestDto paymentReq) {
        CashApiResponse<PayAndHoldResponseDto> response = cashClient.requestBidHold(paymentReq);
        if(!response.isSuccess()) {
            if (response.isChargeFailed()) {
                throw new BadRequestException(FailureCode.WALLET_CHARGE_FAILED);
            }
            throw new BadRequestException(FailureCode.CASH_MODULE_ERROR);
        }
        return response.data();
    }

    public PayAndHoldResponseDto refundBidPayment(PayAndHoldRequestDto refundRequest) {
        CashApiResponse<PayAndHoldResponseDto> response = cashClient.refundBidHold(refundRequest);
        if(!response.isSuccess() || response == null) {}
        return response.data();
    }
}
