package com.back.product.adapter.out.persistence;

import com.back.product.domain.ProductOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductOutboxEventRepository extends JpaRepository<ProductOutboxEvent, Long> {
    Optional<ProductOutboxEvent> findByEventId(String eventId);
}
