package com.back.market.event;

import com.back.common.user.event.UserCreatedEvent;
import com.back.market.adapter.out.CartRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@SpringBootTest
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private MarketUserRepository marketUserRepository;
    @Autowired
    private CartRepository cartRepository;

    @Value("${custom.kafka.topic.user-created}")
    private String topic;

    @Test
    void test1() throws InterruptedException {
        long testId = 300L;
        UserCreatedEvent event = new UserCreatedEvent(
                testId,
                "테스트유저2",
                "김테스트",
                "test2@example.com",
                "서울시 강남구2",
                "010-1234-5678",
                "https://dummyimage.com/100x100/000/fff&text=Test2"
        );
        kafkaTemplate.send(topic, event);
        log.info("Event sent to Kafka topic: UserCreatedEvent (id: {})", testId);

        Thread.sleep(3000);

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
