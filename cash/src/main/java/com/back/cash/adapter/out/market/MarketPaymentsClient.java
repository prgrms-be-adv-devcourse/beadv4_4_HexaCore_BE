package com.back.cash.adapter.out.market;

import com.back.cash.dto.request.PaymentCompletedRequestDto;
import com.back.cash.dto.request.PaymentFailedRequestDto;

public interface MarketPaymentsClient {
    void notifyPaymentCompleted(PaymentCompletedRequestDto dto);
    void notifyPaymentFailed(PaymentFailedRequestDto dto);
}
