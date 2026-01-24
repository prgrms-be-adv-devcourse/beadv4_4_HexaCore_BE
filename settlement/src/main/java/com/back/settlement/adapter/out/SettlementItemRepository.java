package com.back.settlement.adapter.out;

import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementItemStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    boolean existsByOrderId(Long orderId);

    @Query("SELECT DISTINCT si.payeeId FROM SettlementItem si " +
            "WHERE si.settlement IS NULL " +
            "AND si.payeeId IS NOT NULL " +
            "AND si.confirmedAt >= :startAt AND si.confirmedAt <= :endAt")
    List<Long> findDistinctPayeeIdBySettlementIsNullAndConfirmedAtBetween(LocalDateTime startAt, LocalDateTime endAt);

    List<SettlementItem> findByPayeeIdAndSettlementIsNullAndConfirmedAtBetween(Long payeeId, LocalDateTime startAt, LocalDateTime endAt);

    @Query("""
        SELECT si FROM SettlementItem si
        WHERE si.payeeId = :payeeId
        AND (:orderId IS NULL OR si.orderId = :orderId)
        AND (:productId IS NULL OR si.productId = :productId)
        AND (:status IS NULL OR si.status = :status)
        AND (:startDate IS NULL OR si.confirmedAt >= :startDate)
        AND (:endDate IS NULL OR si.confirmedAt <= :endDate)
        ORDER BY si.confirmedAt DESC
        """)
    Page<SettlementItem> findByPayeeIdAndFilters(
            @Param("payeeId") Long payeeId,
            @Param("orderId") Long orderId,
            @Param("productId") Long productId,
            @Param("status") SettlementItemStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
