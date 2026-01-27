package com.back.settlement.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.UnauthorizedException;
import com.back.settlement.app.dto.response.SettlementDashboardResponse;
import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementItemStatus;
import com.back.settlement.domain.SettlementStatus;
import com.back.settlement.mapper.SettlementMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementReadUseCase {
    private final SettlementSupport settlementSupport;
    private final SettlementMapper settlementMapper;

    public List<SettlementResponse> getSettlements(Long sellerId) {
        List<Settlement> settlements = settlementSupport.findBySellerId(sellerId);
        return settlements.stream()
                .map(settlementMapper::toSettlementResponse)
                .toList();
    }

    public SettlementItemResponse getSettlementItem(Long settlementItemId, Long sellerId) {
        SettlementItem settlementItem = settlementSupport.findSettlementItemById(settlementItemId);
        validateSellerAccess(settlementItem, sellerId);
        return settlementMapper.toSettlementItemResponse(settlementItem);
    }

    private void validateSellerAccess(SettlementItem settlementItem, Long sellerId) {
        if (settlementItem.getPayeeId() != null && !settlementItem.getPayeeId().equals(sellerId)) {
            throw new UnauthorizedException(FailureCode.SETTLEMENT_ACCESS_DENIED);
        }
    }

    public Page<SettlementResponse> getSettlementsFilter(
            SettlementStatus status,
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return settlementSupport.findByFilters(status, sellerId, startDate, endDate, pageable)
                .map(settlementMapper::toSettlementResponse);
    }

    public SettlementResponse getSettlementById(Long settlementId) {
        Settlement settlement = settlementSupport.findById(settlementId);
        return settlementMapper.toSettlementResponse(settlement);
    }

    public SettlementDashboardResponse getDashboard() {
        long totalCount = settlementSupport.count();

        Map<String, Long> countByStatus = Arrays.stream(SettlementStatus.values())
                .collect(Collectors.toMap(
                        SettlementStatus::name,
                        settlementSupport::countByStatus
                ));

        return new SettlementDashboardResponse(
                totalCount,
                countByStatus
        );
    }

    public Page<SettlementResponse> getSettlementsBySellerIdAndDateRange(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return settlementSupport.findByFilters(null, sellerId, startDate, endDate, pageable)
                .map(settlementMapper::toSettlementResponse);
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
        return settlementSupport.findItemsByPayeeIdAndFilters(sellerId, orderId, productId, status, startDate, endDate, pageable)
                .map(settlementMapper::toSettlementItemResponse);
    }
}
