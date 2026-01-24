package com.back.settlement.adapter.out;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findBySellerId(Long sellerId);

    List<Settlement> findByStatus(SettlementStatus status, Pageable pageable);

    @Query("""
        SELECT s FROM Settlement s
        WHERE (:status IS NULL OR s.status = :status)
        AND (:sellerId IS NULL OR s.sellerId = :sellerId)
        AND (:startDate IS NULL OR s.createdAt >= :startDate)
        AND (:endDate IS NULL OR s.createdAt <= :endDate)
        ORDER BY s.createdAt DESC
        """)
    Page<Settlement> findByFilters(
            @Param("status") SettlementStatus status,
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    Page<Settlement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(SettlementStatus status);
}
