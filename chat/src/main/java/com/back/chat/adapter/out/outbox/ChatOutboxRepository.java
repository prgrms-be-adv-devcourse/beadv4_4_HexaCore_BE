package com.back.chat.adapter.out.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatOutboxRepository extends JpaRepository<ChatOutbox, Long> {
    Optional<ChatOutbox> findByEventId(UUID eventId);

    @Query(
            value = """
            select *
            from chat_outbox
            where status in ('PENDING', 'FAILED')
              and retry_count < :maxRetry
              and next_attempt_at <= :now
            order by id
            limit :batchSize
            for update skip locked
        """,
            nativeQuery = true
    )
    List<ChatOutbox> lockReadyBatch(
            @Param("now") LocalDateTime now,
            @Param("batchSize") int batchSize,
            @Param("maxRetry") int maxRetry
    );
}
