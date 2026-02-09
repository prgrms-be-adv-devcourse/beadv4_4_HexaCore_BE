package com.back.chat.adapter.out.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatOutboxRepository extends JpaRepository<ChatOutbox, Long>, ChatOutboxRepositoryCustom {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update ChatOutbox o
       set o.status = com.back.chat.adapter.out.outbox.OutboxStatus.SENT,
           o.sentAt = :now
     where o.id = :outboxId
       and o.status = com.back.chat.adapter.out.outbox.OutboxStatus.PROCESSING
""")
    int updateSent(@Param("outboxId") Long outboxId,
                   @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
    update chat_outbox
       set status = 'FAILED',
           retry_count = retry_count + 1,
           last_error = :error,
           next_attempt_at =
             now() + make_interval(
               secs => least(
                 :retryMaxDelaySeconds,
                 :retryBaseDelaySeconds * power(2, retry_count)
               )
             )
     where id = :outboxId
       and status = 'PROCESSING'
    """,
            nativeQuery = true
    )
    int updateFailed(@Param("outboxId") Long outboxId,
                     @Param("error") String error,
                     @Param("retryBaseDelaySeconds") int retryBaseDelaySeconds,
                     @Param("retryMaxDelaySeconds") int retryMaxDelaySeconds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update ChatOutbox o
       set o.status = com.back.chat.adapter.out.outbox.OutboxStatus.DEAD,
           o.deadAt = :now,
           o.lastError = :reason
     where o.id = :outboxId
       and o.status = com.back.chat.adapter.out.outbox.OutboxStatus.FAILED
""")
    int updateDead(@Param("outboxId") Long outboxId,
                   @Param("reason") String reason,
                   @Param("now") LocalDateTime now);
}
