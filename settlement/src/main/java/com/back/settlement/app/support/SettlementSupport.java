package com.back.settlement.app.support;

import com.back.common.code.FailureCode;
import com.back.common.exception.EntityNotFoundException;
import com.back.settlement.adapter.out.SettlementItemRepository;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementSupport {
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    public List<Settlement> findBySellerId(Long sellerId) {
        return settlementRepository.findBySellerId(sellerId);
    }

    public SettlementItem findSettlementItemById(Long settlementItemId) {
        return settlementItemRepository.findById(settlementItemId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.SETTLEMENT_ITEM_NOT_FOUND));
    }
}