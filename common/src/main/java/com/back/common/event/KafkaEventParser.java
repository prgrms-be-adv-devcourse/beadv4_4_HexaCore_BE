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
        String maskedMessage = maskSensitiveInfo(message);
        try {
            // 메시지 전체(Envelope) 역직렬화
            Envelope<T> envelope = jsonMapper.readValue(message, typeReference);

            // 성공 로그: 헤더의 정보를 활용 (예: user-account-updated)
            log.info("[KafkaEventParser] {} 이벤트 수신 성공: {}",
                    envelope.header().eventType(), maskedMessage);

            return envelope.payload();
        } catch (Exception e) {
            log.error("[KafkaEventParser] Payload 역직렬화 실패. 메시지: {}", maskedMessage, e);
            // 역직렬화 실패는 재시도해도 해결되지 않을 가능성이 높으므로, 예외를 던져서 DLT로 전송(MessageConversionException은 Kafka에서 재시도 없이 바로 DLT로 전송됨)
            throw new MessageConversionException("[KafkaEventParser] envelope에서 payload 역직렬화 실패", e);
        }
    }

    /**
     * JSON 메시지에서 민감한 정보를 마스킹하는 메서드
     * @param json 마스킹할 JSON 문자열
     * @return 민감한 정보가 마스킹된 JSON 문자열
     */
    public String maskSensitiveInfo(String json) {
        if (json == null) return null;
        // 1. 마스킹할 패턴 정의 (Case Insensitive 적용)
        // 이메일, 전화번호, 주소 키에 대해 마스킹
        String regex = "\" (email|phoneNumber|phone|address|sellerName|buyerName|name) \"\\s*:\\s*\"[^\"]+\"";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);

        Matcher matcher = pattern.matcher(json);
        StringBuilder sb = new StringBuilder();

        // 2. 매칭되는 부분을 찾아서 교체
        while (matcher.find()) {
            // matcher.group(1)은 email, phoneNumber 등 매칭된 키값
            // 키값은 유지하고 값 부분만 "***"로 마스킹
            matcher.appendReplacement(sb, "\"" + matcher.group(1) + "\":\"***\"");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
