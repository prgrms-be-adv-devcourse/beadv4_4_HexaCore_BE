package com.back.user.kafka;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaConsumeFailLogRepository extends JpaRepository<KafkaConsumeFailLog, Long> {
}
