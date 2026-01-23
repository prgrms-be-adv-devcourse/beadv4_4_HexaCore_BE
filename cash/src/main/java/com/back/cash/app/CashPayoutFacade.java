package com.back.cash.app;

import com.back.cash.app.usecase.CashPayoutUseCase;
import com.back.common.dto.settlement.SettlementPayoutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashPayoutFacade {
    private final CashPayoutUseCase cashPayoutUseCase;

    public void requestPayout(List<SettlementPayoutRequest> requests) {
        for (var req : requests) {
            try {
                cashPayoutUseCase.execute(req);
            } catch (Exception e) {
                log.error("정산 처리 실패 - settlementId: {}, payeeId: {}, reason: {}",
                        req.settlementId(), req.payeeId(), e.getMessage());
            }
        }
    }
}
