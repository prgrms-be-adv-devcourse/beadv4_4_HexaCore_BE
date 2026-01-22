package com.back.market.adapter.out.cash;

import com.back.common.response.CommonResponse;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.request.PaymentCancelRequestDto;
import com.back.market.dto.response.PayAndHoldResponseDto;
import com.back.market.dto.response.PaymentCancelResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!local") //local이 아닐 때만 활성화
@RequiredArgsConstructor
public class CashClientHttp implements CashClient {

    private final CashFeignApi cashFeignApi;

    @Override
    public CommonResponse<PayAndHoldResponseDto> requestBidHold(PayAndHoldRequestDto requestDto) {
        log.info("[CashClientHttp] Real API Call -> Cash Module (Hold)");
        return cashFeignApi.requestBidHold(requestDto);
    }

    @Override
    public CommonResponse<PaymentCancelResponseDto> refundBidHold(PaymentCancelRequestDto requestDto) {
        log.info("[CashClientHttp] Real API Call -> Cash Module (Refund)");
        return cashFeignApi.refundBidHold(requestDto);
    }
}
