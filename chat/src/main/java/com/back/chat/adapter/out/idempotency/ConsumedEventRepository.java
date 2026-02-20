package com.back.chat.adapter.out.idempotency;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ConsumedEventRepository extends Repository<ConsumedEvent, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO consumed_event (event_id, event_type, consumed_at)
        VALUES (:eventId, :eventType, :consumedAt)
        ON CONFLICT (event_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(UUID eventId,
                       String eventType,
                       LocalDateTime consumedAt);
}
