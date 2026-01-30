package com.back.notification.adapter.out;

import com.back.notification.domain.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    @Query("""
    SELECT pa
    FROM PriceAlert pa
    WHERE pa.productId = :productId
      AND pa.targetPrice >= :currentPrice
      AND (
            pa.triggeredAt IS NULL
         OR pa.triggeredAt <= :threshold
      )
""")
    List<PriceAlert> findEligibleAlerts(
            @Param("productId") Long productId,
            @Param("currentPrice") BigDecimal currentPrice,
            @Param("threshold") LocalDateTime threshold
    );

    List<PriceAlert> findByUserId(Long userId);
}
