package com.back.market.event;

import com.back.common.market.event.UserCreatedEvent;
import com.back.market.adapter.out.MarketUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@SpringBootTest
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private MarketUserRepository marketUserRepository;

    @Test
    void test1() throws InterruptedException {
        long testId = 101L;
        UserCreatedEvent event = new UserCreatedEvent(
                testId,
                "테스트유저2",
                "test2@example.com",
                "서울시 강남구2",
                "010-1234-5678",
                "https://dummyimage.com/100x100/000/fff&text=Test2"
        );
        kafkaTemplate.send("${custom.kafka.topic.user-created}", event);
        log.info("Event sent to Kafka topic: UserCreatedEvent (id: {})", testId);
        Thread.sleep(3000);
        boolean exists = marketUserRepository.existsById(testId);
        if(exists) {
            log.info("Data saved successfully. ID: {}", testId);
        } else {
            log.error("Data not saved. ID: {}", testId);
        }
    }

}
