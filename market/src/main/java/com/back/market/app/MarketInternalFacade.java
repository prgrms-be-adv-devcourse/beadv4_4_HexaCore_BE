package com.back.market.app;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.market.app.usecase.ConfirmPaymentUseCase;
import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import com.back.market.app.usecase.GetSettlementDataUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketInternalFacade {
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final GetSettlementDataUseCase getSettlementDataUseCase;

    /**
     * Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정
     * @param requestDto PaymentCompletedRequestDto
     */
    @Transactional
    public boolean confirmPayment(PaymentCompletedRequestDto requestDto) {
        return confirmPaymentUseCase.confirmPayment(requestDto);
    }

    public List<SettlementTargetOrder> getSettlementData(YearMonth targetMonth, int page, int size) {
        return getSettlementDataUseCase.getSettlementData(targetMonth,  page, size);
    }
}
