package com.back.settlement.app.facade;

import com.back.settlement.app.dto.response.SettlementDashboardResponse;
import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementLogResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.usecase.SettlementLogReadUseCase;
import com.back.settlement.app.usecase.SettlementReadUseCase;
import com.back.settlement.domain.SettlementItemStatus;
import com.back.settlement.domain.SettlementStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<SettlementResponse> getSettlementsFilter(
            SettlementStatus status,
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return settlementReadUseCase.getSettlementsFilter(status, sellerId, startDate, endDate, pageable);
    }

    public SettlementResponse getSettlementById(Long settlementId) {
        return settlementReadUseCase.getSettlementById(settlementId);
    }

    public SettlementDashboardResponse getDashboard() {
        return settlementReadUseCase.getDashboard();
    }

    public Page<SettlementResponse> getSettlementsBySellerIdAndDateRange(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return settlementReadUseCase.getSettlementsBySellerIdAndDateRange(sellerId, startDate, endDate, pageable);
    }

    public Page<SettlementItemResponse> getSettlementItemsBySellerIdAndFilters(
            Long sellerId,
            Long orderId,
            Long productId,
            SettlementItemStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return settlementReadUseCase.getSettlementItemsBySellerIdAndFilters(sellerId, orderId, productId, status, startDate, endDate, pageable);
    }
}
