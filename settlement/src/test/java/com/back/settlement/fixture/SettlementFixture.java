package com.back.settlement.fixture;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementFixture {

    public static Settlement createSettlement(
            Long id,
            Long sellerId,
            SettlementStatus status
    ) {
        return createSettlement(
                id, sellerId, status,
                null,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now(),
                null,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(90000)
        );
    }

    public static Settlement createSettlement(
            Long id,
            Long sellerId,
            SettlementStatus status,
            LocalDateTime expectedAt,
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime completedAt,
            BigDecimal totalSalesAmount,
            BigDecimal totalFeeAmount,
            BigDecimal totalNetAmount
    ) {
        try {
            Constructor<Settlement> constructor = Settlement.class.getDeclaredConstructor(
                    Long.class, Long.class, SettlementStatus.class,
                    LocalDateTime.class, LocalDateTime.class, LocalDateTime.class, LocalDateTime.class,
                    BigDecimal.class, BigDecimal.class, BigDecimal.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    id, sellerId, status, expectedAt, startAt, endAt, completedAt,
                    totalSalesAmount, totalFeeAmount, totalNetAmount
            );
        } catch (Exception e) {
            throw new RuntimeException("Settlement 생성 실패", e);
        }
    }

    public static Settlement createCompletedSettlement(Long id, Long sellerId) {
        return createSettlement(
                id, sellerId, SettlementStatus.COMPLETED,
                LocalDateTime.of(2024, 2, 15, 0, 0),
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 31, 23, 59),
                LocalDateTime.of(2024, 2, 15, 10, 30),
                BigDecimal.valueOf(1500000),
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(1350000)
        );
    }

    public static Settlement createPendingSettlement(Long id, Long sellerId) {
        return createSettlement(id, sellerId, SettlementStatus.PENDING);
    }
}
