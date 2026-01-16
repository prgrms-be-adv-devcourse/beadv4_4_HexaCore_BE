package com.back.settlement.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.EntityNotFoundException;
import com.back.common.exception.UnauthorizedException;
import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.mapper.SettlementMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementUseCase {
    private final SettlementSupport settlementSupport;
    private final SettlementMapper settlementMapper;

    public List<SettlementResponse> getSettlements(Long sellerId) {
        List<Settlement> settlements = settlementSupport.findBySellerId(sellerId);
        return settlements
                .stream()
                .map(settlementMapper::toSettlementResponse)
                .toList();
    }

    public SettlementItemResponse getSettlementItem(Long settlementItemId, Long sellerId) {
        SettlementItem settlementItem = settlementSupport.findSettlementItemById(settlementItemId);
        validateSellerAccess(settlementItem, sellerId);
        return settlementMapper.toSettlementItemResponse(settlementItem);
    }

    private void validateSellerAccess(SettlementItem settlementItem, Long sellerId) {
        if (!settlementItem.getSellerId().equals(sellerId)) {
            throw new UnauthorizedException(FailureCode.SETTLEMENT_ACCESS_DENIED);
        }
    }
}