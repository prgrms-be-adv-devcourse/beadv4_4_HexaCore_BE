package com.back.chat.adapter.out.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaConsumeFailLogRepository extends JpaRepository <KafkaConsumeFailLog, Long> {
}
