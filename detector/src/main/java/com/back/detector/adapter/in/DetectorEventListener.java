package com.back.detector.adapter.in;

import com.back.common.event.KafkaEventParser;
import com.back.detector.app.event.payload.UserCreatedEvent;
import com.back.detector.app.HijackDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class DetectorEventListener {

    private final HijackDetector hijackDetector;
    private final KafkaEventParser kafkaEventParser;

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleUserCreated(String message) {
        UserCreatedEvent event = kafkaEventParser.extractPayload(message, new TypeReference<>() {});

        if (event.ipAddress() == null || event.ipAddress().isEmpty()) {
            log.warn("[회원가입 IP 이벤트] IP 정보 없음 - 스킵, userId: {}", event.id());
            return;
        }
        // 회원가입 IP를 안전한 IP로 등록 (쿨다운 없음)

        hijackDetector.registerInitialIp(event.id(), event.ipAddress());

        log.info("[회원가입 초기 IP 등록 완료] userId: {}, IP: {} (쿨다운 없음)",
                event.id(), event.ipAddress());
    }
}
