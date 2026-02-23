package com.back.market.event;

import com.back.common.event.KafkaEventParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class KafkaEventParserMaskingTests {
    @Autowired
    private KafkaEventParser kafkaEventParser;

    @Test
    @DisplayName("개인정보가 포함된 메시지 역직렬화 실패 시 로그에 마스킹되어 남는지 확인한다")
    void maskSensitiveInfo_Test() {
        // 1. 역직렬화가 반드시 실패하도록 구조를 깼지만, 개인정보는 포함된 JSON
        // (일부러 중괄호를 생략하거나 타입을 틀리게 구성)
        String sensitiveMessage = """
        {
          "header": { "eventId": "error-001", "eventType": "user-created" },
          "payload": {
            "name": "홍길동",
            "Email": "test1234@naver.com",
            "phoneNumber": "010-1234-5678",
            "address": "서울시 강남구 테헤란로 123"
          }
        -- 강제로 JSON 구조를 깨뜨림 --
        """;

        // 2. 실행 및 예외 확인
        // 이 과정에서 KafkaEventParser 내부 catch 블록의 log.error가 실행됨
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            kafkaEventParser.extractPayload(sensitiveMessage, new tools.jackson.core.type.TypeReference<>() {});
        });

        // 3. 콘솔 로그 확인
        // [KafkaEventParser] Payload 역직렬화 실패. 메시지: ...
        // 부분에 "email":"***" 처럼 별표로 나오는지 직접 확인합니다.
    }
}
