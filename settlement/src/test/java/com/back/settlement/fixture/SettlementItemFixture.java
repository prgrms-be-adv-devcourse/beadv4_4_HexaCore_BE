package com.back.settlement.fixture;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementItemStatus;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementItemFixture {

    public static SettlementItem createSettlementItem(Long id, Long sellerId) {
        return createSettlementItem(
                id, null, 1L, 1L, 1L, sellerId,
                SettlementItemStatus.INCLUDED,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(45000),
                LocalDateTime.now()
        );
    }

    public static SettlementItem createSettlementItem(
            Long id,
            Settlement settlement,
            Long orderId,
            Long productId,
            Long buyerId,
            Long sellerId,
            SettlementItemStatus status,
            BigDecimal salesAmount,
            BigDecimal feeAmount,
            BigDecimal netAmount,
            LocalDateTime transactionAt
    ) {
        try {
            Constructor<SettlementItem> constructor = SettlementItem.class.getDeclaredConstructor(
                    Long.class, Settlement.class, Long.class, Long.class, Long.class, Long.class,
                    SettlementItemStatus.class, BigDecimal.class, BigDecimal.class, BigDecimal.class,
                    LocalDateTime.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    id, settlement, orderId, productId, buyerId, sellerId,
                    status, salesAmount, feeAmount, netAmount, transactionAt
            );
        } catch (Exception e) {
            throw new RuntimeException("SettlementItem 생성 실패", e);
        }
    }

    public static SettlementItem createIncludedItem(Long id, Long sellerId) {
        return createSettlementItem(id, sellerId);
    }

    public static SettlementItem createRefundedItem(Long id, Long sellerId) {
        return createSettlementItem(
                id, null, 1L, 1L, 1L, sellerId,
                SettlementItemStatus.REFUNDED,
                BigDecimal.valueOf(30000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(27000),
                LocalDateTime.now()
        );
    }

    public static SettlementItem createCanceledItem(Long id, Long sellerId) {
        return createSettlementItem(
                id, null, 1L, 1L, 1L, sellerId,
                SettlementItemStatus.CANCELED,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(45000),
                LocalDateTime.now()
        );
    }
}