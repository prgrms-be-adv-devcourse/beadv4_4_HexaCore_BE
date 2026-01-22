package com.back.settlement.app.usecase;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.settlement.adapter.out.SettlementItemRepository;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.domain.SettlementItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementItemAddUseCase {
    private final SettlementItemRepository settlementItemRepository;
    private final SettlementSupport settlementSupport;

    @Value("${settlement.system-payee-id}")
    private Long systemPayeeId;

    // 정산 아이템
    @Transactional
    public void add(SettlementTargetOrder request) {
        if (settlementSupport.existsByOrderId(request.orderId())) {
            log.warn("이미 처리된 주문입니다. orderId={}", request.orderId());
            return;
        }
        List<SettlementItem> items = SettlementItem.createSettlementItem(request, systemPayeeId);
        settlementItemRepository.saveAll(items);
        log.info("정산 아이템 추가 완료. orderId={}, 생성건수={}", request.orderId(), items.size());
    }
}
