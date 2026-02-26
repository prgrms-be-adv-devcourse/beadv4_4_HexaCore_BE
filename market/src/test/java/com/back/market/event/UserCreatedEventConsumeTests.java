package com.back.market.event;

import com.back.common.event.Envelope;
import com.back.market.adapter.out.CartRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketUser;
import com.back.market.event.payload.UserCreatedPayload;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@SpringBootTest
public class UserCreatedEventConsumeTests {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private MarketUserRepository marketUserRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.user-account-created}")
    private String topic;

    @Test
    void test1() throws Exception {
        long testId = 300L;
        
        // 1. 페이로드 생성
        UserCreatedPayload userCreatedPayload = new UserCreatedPayload(
                testId,
                "김테스트", // 이름
                "test2@example.com", // 이메일
                "서울시 강남구2", // 주소
                "010-1234-5678" // 전화번호
        );

        // 2. 페이로드를 envelope에 담음
        Envelope<UserCreatedPayload> envelope = Envelope.of("UserCreatedEvent", userCreatedPayload);

        // 3. envelope를 json 문자열로 직렬화
        //String message = jsonMapper.writeValueAsString(envelope);
        
        // 4. kafka로 메시지 전송
        kafkaTemplate.send(topic, envelope);
        log.info("Event sent to Kafka topic: UserCreatedPayload (id: {})", testId);

        Thread.sleep(3000); // 시간차 두기

        MarketUser user = marketUserRepository.findById(testId).orElse(null);

        if(user != null) {
            log.info("1. 회원 데이터 복제 성공. ID: {}", testId);

            boolean cartExists = cartRepository.existsByMarketUser(user);
            if(cartExists) {
                log.info("2. 장바구니 생성 성공. User ID: {}", testId);
            } else {
                log.error("2. 장바구니가 생성되지 않았습니다. User ID: {}", testId);
            }

        } else {
            log.error("1. 회원 데이터 복제 실패. ID: {}", testId);
        }
    }

}
