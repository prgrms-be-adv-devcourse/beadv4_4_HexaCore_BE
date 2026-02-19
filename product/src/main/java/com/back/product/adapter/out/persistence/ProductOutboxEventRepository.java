package com.back.product.adapter.out.persistence;

import com.back.product.domain.ProductOutboxEvent;
import com.back.product.dto.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductOutboxEventRepository extends JpaRepository<ProductOutboxEvent, Long> {
    Optional<ProductOutboxEvent> findByEventId(String eventId);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE ProductOutboxEvent p SET p.status = :status
                WHERE p.eventId = :eventId
                AND (p.status = 'INIT' OR (
                    (p.status = 'FAILED' OR p.status = 'DEAD') AND p.createdAt < :expiredAt))
            """)
    int updateStatus(
            @Param("eventId") String eventId,
            @Param("status") OutboxEventStatus status,
            @Param("expiredAt") LocalDateTime expiredAt);
}
