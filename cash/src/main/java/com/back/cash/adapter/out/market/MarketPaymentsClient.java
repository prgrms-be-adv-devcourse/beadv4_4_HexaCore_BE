package com.back.cash.adapter.out.market;


import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import com.back.common.dto.cash.request.PaymentFailedRequestDto;

public interface MarketPaymentsClient {
    void notifyPaymentCompleted(PaymentCompletedRequestDto dto);
    void notifyPaymentFailed(PaymentFailedRequestDto dto);
}
