package com.back.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventParser {

    private final JsonMapper jsonMapper;

    /**
     * Envelope에서 페이로드를 추출하는 유틸리티 메서드
     * @param message Kafka에서 수신한 원본 메시지(Envelope 형태의 JSON 문자열)
     * @param typeReference 역직렬화할 Envelope의 타입 정보
     * @return Envelope에서 추출한 페이로드 객체
     * @param <T> KafkaPayload를 구현한 페이로드 타입
     */
    public <T extends KafkaPayload> T extractPayload(
            String message,
            TypeReference<Envelope<T>> typeReference
    ) {
        try {
            return jsonMapper.readValue(message, typeReference).payload();
        } catch (Exception e) {
            log.error("[KafkaEventParser] Payload 역직렬화 실패. 원본 메시지: {}", message, e);
            // 역직렬화 실패는 재시도해도 해결되지 않을 가능성이 높으므로, 예외를 던져서 DLT로 전송(MessageConversionException은 Kafka에서 재시도 없이 바로 DLT로 전송됨)
            throw new MessageConversionException("[KafkaEventParser] envelope에서 payload 역직렬화 실패", e);
        }
    }
}
