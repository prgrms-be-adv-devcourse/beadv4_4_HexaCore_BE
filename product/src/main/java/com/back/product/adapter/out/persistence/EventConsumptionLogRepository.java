package com.back.product.adapter.out.persistence;

import com.back.product.domain.EventConsumptionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventConsumptionLogRepository extends JpaRepository<EventConsumptionLog, Long> {
    Optional<EventConsumptionLog> findByEventId(String eventId);
}
