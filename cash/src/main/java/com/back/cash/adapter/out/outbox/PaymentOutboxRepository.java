package com.back.cash.adapter.out.outbox;

import com.back.cash.domain.outbox.PaymentOutbox;
import com.back.cash.domain.outbox.enums.OutboxStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, Long> {

    List<PaymentOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Limit limit);

    @Query("SELECT o FROM PaymentOutbox o " +
            "WHERE o.status = 'FAILED' AND o.retryCount < :maxRetry AND o.nextRetryAt <= :now " +
            "ORDER BY o.createdAt ASC")
    List<PaymentOutbox> findRetryable(@Param("maxRetry") int maxRetry,
                                      @Param("now") LocalDateTime now,
                                      Limit limit);
}
