package com.back.user.kafka;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaChatDltPublishedLogRepository extends JpaRepository<KafkaChatDltPublishedLog, Long> {
}
