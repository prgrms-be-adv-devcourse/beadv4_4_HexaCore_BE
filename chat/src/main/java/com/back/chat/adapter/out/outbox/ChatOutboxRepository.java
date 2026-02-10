package com.back.chat.adapter.out.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatOutboxRepository extends JpaRepository<ChatOutbox, Long> {
    @Modifying(clearAutomatically = true)
    @Query(value = """
update chat_outbox
   set status = 'SENT',
       sent_at = :now
 where id = :outboxId
   and status = 'PROCESSING'
""", nativeQuery = true)
    int markSent(@Param("outboxId") Long outboxId,
                 @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query(value = """
        update chat_outbox
           set status = 'PROCESSING',
               processing_started_at = :now
         where id = :outboxId
           and status = 'PENDING'
        """, nativeQuery = true)
    int claimOneById(@Param("outboxId") Long outboxId,
                     @Param("now") LocalDateTime now);

    @Modifying
    @Query(value = """
update chat_outbox
   set status = 'FAILED',
       retry_count = retry_count + 1,
       last_error = :error,
       next_attempt_at = now() + make_interval(secs => :baseDelaySeconds)
 where id = :outboxId
   and status = 'PROCESSING'
""", nativeQuery = true)
    int markInitialFailed(
            @Param("outboxId") Long outboxId,
            @Param("error") String error,
            @Param("baseDelaySeconds") int baseDelaySeconds
    );

    @Query(value = """
        WITH cte AS (
            SELECT id
              FROM chat_outbox
             WHERE status = 'FAILED'
               AND next_attempt_at <= :now
             ORDER BY next_attempt_at ASC, id ASC
             LIMIT :batchSize
             FOR UPDATE SKIP LOCKED
        )
        UPDATE chat_outbox o
           SET status = 'PROCESSING',
               processing_started_at = :now
         WHERE o.id IN (SELECT id FROM cte)
        RETURNING o.id
        """, nativeQuery = true)
    List<Long> claimFailedBatch(@Param("now") LocalDateTime now,
                                @Param("batchSize") int batchSize);

    @Query(value = """
select *
  from chat_outbox
 where id = any(:ids)
 order by next_attempt_at nulls last, id asc
""", nativeQuery = true)
    List<ChatOutbox> findByIdInOrderByNextAttemptAtAscIdAsc(@Param("ids") Long[] ids);

    @Modifying(clearAutomatically = true)
    @Query(value = """
UPDATE chat_outbox
SET
  retry_count = retry_count + 1,
  last_error = :lastError,
  status =
    CASE
      WHEN (retry_count + 1) >= :maxRetry THEN 'DEAD'
      ELSE 'FAILED'
    END,
  next_attempt_at =
    CASE
      WHEN (retry_count + 1) >= :maxRetry THEN NULL
      ELSE
        :now + make_interval(
          secs => LEAST(
            :retryMaxDelaySeconds,
            (:retryBaseDelaySeconds * POWER(2, (retry_count + 1) - 1))::bigint
          )
        )
    END,
  dead_at =
    CASE
      WHEN (retry_count + 1) >= :maxRetry THEN :now
      ELSE dead_at
    END,
  processing_started_at = NULL
WHERE id = :outboxId
  AND status = 'PROCESSING'
RETURNING status
""", nativeQuery = true)
    String markFailedOrDeadAtomic(
            @Param("outboxId") Long outboxId,
            @Param("lastError") String lastError,
            @Param("now") LocalDateTime now,
            @Param("maxRetry") int maxRetry,
            @Param("retryBaseDelaySeconds") int retryBaseDelaySeconds,
            @Param("retryMaxDelaySeconds") int retryMaxDelaySeconds
    );
}
