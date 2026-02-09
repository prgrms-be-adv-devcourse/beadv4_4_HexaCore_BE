package com.back.settlement.fixture;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementEventType;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementItemStatus;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementItemFixture {

    public static SettlementItem createCollectedItem(Long id, Long payeeId) {
        return createSettlementItem(
                id, null, 1L, 1L, 1L, payeeId, "TestSeller",
                BigDecimal.valueOf(100000), SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
                SettlementItemStatus.COLLECTED, LocalDateTime.now()
        );
    }

    public static SettlementItem createIncludedItem(Long id, Long payeeId) {
        return createSettlementItem(
                id, null, 1L, 1L, 1L, payeeId, "TestSeller",
                BigDecimal.valueOf(100000), SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
                SettlementItemStatus.INCLUDED, LocalDateTime.now()
        );
    }

    public static SettlementItem createSettlementItem(
            Long id,
            Settlement settlement,
            Long orderId,
            Long productId,
            Long payerId,
            Long payeeId,
            String sellerName,
            BigDecimal amount,
            SettlementEventType eventType,
            SettlementItemStatus status,
            LocalDateTime transactionAt
    ) {
        try {
            Constructor<SettlementItem> constructor = SettlementItem.class.getDeclaredConstructor(
                    Long.class, Settlement.class, Long.class, Long.class, Long.class, Long.class, String.class,
                    BigDecimal.class, SettlementEventType.class, SettlementItemStatus.class, LocalDateTime.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    id, settlement, orderId, productId, payerId, payeeId, sellerName,
                    amount, eventType, status, transactionAt
            );
        } catch (Exception e) {
            throw new RuntimeException("SettlementItem 생성 실패", e);
        }
    }
}
