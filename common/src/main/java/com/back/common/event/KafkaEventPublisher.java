package com.back.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaEventParser kafkaEventParser;

    public void publish(EventName event) {
        kafkaTemplate.send(event.getEventName(), event);
    }

    public void publish(String topic, EventName event) {
        kafkaTemplate.send(topic, event);
    }

    /**
     * 마스킹 및 에러 핸들링을 포함한 공통 발행 템플릿 메서드
     */
    public <T extends KafkaPayload> void publish(String topic, Envelope<T> envelope) {
        try {
            // 1. 이벤트 발행
            kafkaTemplate.send(topic, envelope);

            // 2. 로그 마스킹 처리 (개인정보 보호)
            // envelope.toString() 결과물 내의 PII를 KafkaEventParser의 로직으로 마스킹합니다.
            String maskedLog = kafkaEventParser.maskSensitiveInfo(envelope.toString());
            log.info("[KafkaEventPublisher] 이벤트 발행 성공 - Topic: {}, Message: {}", topic, maskedLog);

        } catch (Exception e) {
            String maskedErrorLog = kafkaEventParser.maskSensitiveInfo(envelope.toString());
            log.error("[KafkaEventPublisher] 이벤트 발행 실패 - Topic: {}, Message: {}, 사유: {}",
                    topic, maskedErrorLog, e.getMessage());

            // 여기서 공통 예외를 던지거나 런타임 예외로 래핑합니다.
            throw new RuntimeException("Kafka 발행 중 오류 발생", e);
        }
    }
}
