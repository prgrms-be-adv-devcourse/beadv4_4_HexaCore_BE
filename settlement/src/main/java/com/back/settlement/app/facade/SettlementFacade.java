package com.back.settlement.app.facade;

import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementLogResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.usecase.SettlementLogReadUseCase;
import com.back.settlement.app.usecase.SettlementReadUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementFacade {
    private final SettlementReadUseCase settlementReadUseCase;
    private final SettlementLogReadUseCase settlementLogReadUseCase;

    public List<SettlementResponse> getSettlements(Long sellerId) {
        return settlementReadUseCase.getSettlements(sellerId);
    }

    public SettlementItemResponse getSettlementItem(Long settlementItemId, Long sellerId) {
        return settlementReadUseCase.getSettlementItem(settlementItemId, sellerId);
    }

    public List<SettlementLogResponse> getLogsBySettlementId(Long settlementId) {
        return settlementLogReadUseCase.getLogsBySettlementId(settlementId);
    }

    public List<SettlementLogResponse> getAllLogs() {
        return settlementLogReadUseCase.getAllLogs();
    }

}
