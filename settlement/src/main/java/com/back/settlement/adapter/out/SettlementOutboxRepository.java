package com.back.settlement.adapter.out;

import com.back.settlement.domain.outbox.SettlementOutboxEvent;
import com.back.settlement.domain.outbox.SettlementOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementOutboxRepository extends JpaRepository<SettlementOutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM SettlementOutboxEvent e WHERE e.id = :id")
    Optional<SettlementOutboxEvent> findByIdWithPessimisticWriteLock(@Param("id") Long id);

    @Query(value = """
            SELECT id FROM settlement_outbox_event
            WHERE status = :status
            ORDER BY create_date ASC LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Long> findPendingIds(@Param("status") String status, @Param("batchSize") int batchSize);

    @Query(value = """
            SELECT id FROM settlement_outbox_event
            WHERE status = :status AND retry_count < :maxRetry AND next_attempt_at <= :now
            ORDER BY next_attempt_at ASC, id ASC LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Long> findFailedIds(@Param("status") String status,
                             @Param("now") LocalDateTime now,
                             @Param("batchSize") int batchSize,
                             @Param("maxRetry") int maxRetry);

    @Query(value = """
            SELECT id FROM settlement_outbox_event
            WHERE status = :status AND updated_at <= :threshold
            ORDER BY updated_at ASC
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Long> findTimedOutProcessingIds(@Param("status") String status,
                                         @Param("threshold") LocalDateTime threshold);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SettlementOutboxEvent e WHERE e.status = :status AND e.updatedAt <= :before")
    int deleteOldSent(@Param("status") SettlementOutboxStatus status,
                      @Param("before") LocalDateTime before);

}
