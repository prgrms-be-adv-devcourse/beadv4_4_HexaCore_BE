package com.back.market.event;

import com.back.market.adapter.in.event.MarketKafkaEventListener;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserUpdatedEventConsumeTests {

    @Autowired
    private MarketKafkaEventListener listener;

    @Autowired
    private MarketUserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 테스트용 기존 유저 저장
        MarketUser existingUser = MarketUser.builder()
                .id(1L)
                .name("Old Name")
                .email("old@example.com")
                .address("Old Address")
                .phone("010-0000-0000")
                .build();
        userRepository.save(existingUser);
    }

    @Test
    @DisplayName("유저 수정 이벤트 수신 시 실제 DB의 사용자 정보가 변경되는지 검증한다")
    void consumeUserUpdatedEvent_Success() {
        // 1. 수정용 JSON 메시지 준비 (일부러 대소문자를 섞어서 마스킹 로직도 고려)
        String updateMessage = """
        {
          "header": { "eventId": "user-upd-001", "eventType": "user-account-updated" },
          "payload": {
            "id": 1,
            "name": "New Name",
            "email": "new@example.com",
            "address": "New Seoul Address",
            "phone": "010-1234-5678"
          }
        }
        """;

        // 2. 이벤트 컨슈밍
        listener.consumeUserUpdatedEvent(updateMessage);

        // 3. 결과 검증
        MarketUser updatedUser = userRepository.findById(1L).orElseThrow();

        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getAddress()).isEqualTo("New Seoul Address");
        assertThat(updatedUser.getPhone()).isEqualTo("010-1234-5678");

        // 이메일은 비즈니스 로직상 업데이트 대상이 아니므로 기존 값 유지 확인
        assertThat(updatedUser.getEmail()).isEqualTo("old@example.com");
    }

    @Test
    @DisplayName("잘못된 형식의 유저 메시지 수신 시 PII가 마스킹되어 로그에 남는지 확인 (콘솔 출력 확인용)")
    void consumeUserUpdatedEvent_MaskingCheck() {
        // 일부러 JSON 구조를 깨뜨려서 역직렬화 예외 발생 유도
        String brokenMessage = """
        {
          "header": { "eventId": "mask-test-001" },
          "payload": {
            "id": 1,
            "email": "secret-user@naver.com",
            "address": "서울시 민감한 주소"
          }
        -- 강제 에러 유발 --
        """;

        // 실행 시 KafkaEventParser에서 마스킹된 로그가 찍히는지 콘솔 확인
        try {
            listener.consumeUserUpdatedEvent(brokenMessage);
        } catch (Exception e) {
            System.out.println("예외 발생(정상): " + e.getMessage());
        }
    }
}