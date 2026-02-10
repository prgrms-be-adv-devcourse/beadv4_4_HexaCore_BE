package com.back.user.kafka;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserConsumedEventRepository extends JpaRepository<UserConsumedEvent, UUID> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO user_consumed_event (event_id, event_type, consumed_at)
        VALUES (:eventId, :eventType, :consumedAt)
        ON CONFLICT (event_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") UUID eventId,
                       @Param("eventType") String eventType,
                       @Param("consumedAt") LocalDateTime consumedAt);
}
