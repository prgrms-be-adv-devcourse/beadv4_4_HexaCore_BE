package com.back.settlement.app.facade;

import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.usecase.SettlementUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementFacade {
    private final SettlementUseCase settlementUseCase;

    public List<SettlementResponse> getSettlements(Long sellerId) {
        return settlementUseCase.getSettlements(sellerId);
    }

    public SettlementItemResponse getSettlementItem(Long settlementItemId, Long sellerId) {
        return settlementUseCase.getSettlementItem(settlementItemId, sellerId);
    }
}