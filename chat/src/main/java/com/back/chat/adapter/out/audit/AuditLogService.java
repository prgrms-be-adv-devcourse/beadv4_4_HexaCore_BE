package com.back.chat.adapter.out.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final KafkaConsumeFailLogRepository kafkaConsumeFailLogRepository;

    @Transactional
    public void saveLog(KafkaConsumeFailLogCommand cmd) {
        kafkaConsumeFailLogRepository.save(KafkaConsumeFailLog.fromCommand(cmd));
    }
}
