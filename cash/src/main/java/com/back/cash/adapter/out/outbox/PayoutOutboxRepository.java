package com.back.cash.adapter.out.outbox;

import com.back.cash.domain.outbox.PayoutOutbox;
import com.back.cash.domain.outbox.enums.OutboxStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PayoutOutboxRepository extends JpaRepository<PayoutOutbox, Long> {

    List<PayoutOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Limit limit);

    @Query("SELECT o FROM PayoutOutbox o " +
            "WHERE o.status = 'FAILED' AND o.retryCount < :maxRetry AND o.nextRetryAt <= :now " +
            "ORDER BY o.createdAt ASC")
    List<PayoutOutbox> findRetryable(@Param("maxRetry") int maxRetry,
                                     @Param("now") LocalDateTime now,
                                     Limit limit);
}
