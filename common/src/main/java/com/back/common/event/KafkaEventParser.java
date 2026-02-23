package com.back.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String maskedMessage = maskSensitiveInfo(message);
            log.error("[KafkaEventParser] Payload 역직렬화 실패. 메시지: {}", maskedMessage, e);
            // 역직렬화 실패는 재시도해도 해결되지 않을 가능성이 높으므로, 예외를 던져서 DLT로 전송(MessageConversionException은 Kafka에서 재시도 없이 바로 DLT로 전송됨)
            throw new MessageConversionException("[KafkaEventParser] envelope에서 payload 역직렬화 실패", e);
        }
    }

    private String maskSensitiveInfo(String json) {
        if (json == null) return null;
        // 1. 마스킹할 패턴 정의 (Case Insensitive 적용)
        // 이메일, 전화번호, 주소 키에 대해 마스킹
        String regex = "\" (email|phoneNumber|address) \"\\s*:\\s*\"[^\"]+\"";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);

        Matcher matcher = pattern.matcher(json);
        StringBuilder sb = new StringBuilder();

        // 2. 매칭되는 부분을 찾아서 교체
        while (matcher.find()) {
            // matcher.group(1)은 email, phoneNumber 등 매칭된 키값입니다.
            // 키값은 유지하고 값 부분만 "***"로 마스킹합니다.
            matcher.appendReplacement(sb, "\"" + matcher.group(1) + "\":\"***\"");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
