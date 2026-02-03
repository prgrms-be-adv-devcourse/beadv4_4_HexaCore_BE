package com.back.chat.adapter.out.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatOutboxRepository extends JpaRepository<ChatOutbox, Long> {
    Optional<ChatOutbox> findByEventId(UUID eventId);
}
