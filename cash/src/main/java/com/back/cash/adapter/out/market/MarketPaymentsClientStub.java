package com.back.cash.adapter.out.market;

import com.back.cash.dto.request.PaymentCompletedRequestDto;
import com.back.cash.dto.request.PaymentFailedRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class MarketPaymentsClientStub implements MarketPaymentsClient {

    @Override
    public void notifyPaymentCompleted(PaymentCompletedRequestDto dto) {
        log.info("[MarketPaymentsClientStub] notifyPaymentCompleted: relType={}, relId={}, totalAmount={}",
                dto.relType(), dto.relId(), dto.totalAmount());
    }

    @Override
    public void notifyPaymentFailed(PaymentFailedRequestDto dto) {
        log.info("[MarketPaymentsClientStub] notifyPaymentFailed: relType={}, relId={}",
                dto.relType(), dto.relId());
    }
}

