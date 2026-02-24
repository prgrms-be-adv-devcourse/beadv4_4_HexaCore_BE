package com.back.common.event;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaEventParser kafkaEventParser;
    private final JsonMapper jsonMapper;

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


            // 로그 마스킹 처리 (toString 대신 jsonMapper 사용)
            String jsonString = jsonMapper.writeValueAsString(envelope);
            String maskedLog = kafkaEventParser.maskSensitiveInfo(jsonString);

            // 카프카 전송 (예전처럼 객체 그대로 가볍게 던지기)
            kafkaTemplate.send(topic, envelope);

            log.info("[KafkaEventPublisher] 이벤트 발행 - Topic: {}, Message: {}", topic, maskedLog);

        } catch (Exception e) {
            // 실패 시 원본 로그 없이 심플하게 에러 메시지만 남김
            log.error("[KafkaEventPublisher] 이벤트 발행 실패 - Topic: {}, 사유: {}", topic, e.getMessage(), e);
            throw new CustomException(FailureCode.EVENT_PUBLISH_FAILED);
        }
    }
}
