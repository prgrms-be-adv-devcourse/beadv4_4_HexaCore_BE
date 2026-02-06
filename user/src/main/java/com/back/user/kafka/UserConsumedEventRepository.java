package com.back.user.kafka;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserConsumedEventRepository extends JpaRepository<UserConsumedEvent, UUID> {
}
