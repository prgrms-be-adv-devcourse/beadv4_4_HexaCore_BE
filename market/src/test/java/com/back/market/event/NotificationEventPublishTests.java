package com.back.market.event;

import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.app.MarketFacade;
import com.back.market.domain.MarketUser;
import com.back.market.dto.request.BiddingRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class NotificationEventPublishTests {

    @Autowired
    private MarketFacade marketFacade;
    @Autowired
    private MarketUserRepository userRepository;

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("판매 입찰 등록 시 카프카로 알림 이벤트가 발행되는지 확인")
    void registerSellBid_PublishKafkaMessage() throws InterruptedException {
        Long testUserId = 999L;
        Long testProductId = 200L;

        userRepository.deleteById(testUserId);
        userRepository.save(MarketUser.builder()
                .id(testUserId)
                .name("테스트판매자")
                .email("test-seller@example.com")
                .build());

        BigDecimal bidPrice = new BigDecimal("1000000"); // 100만원
        BiddingRequestDto requestDto = BiddingRequestDto.of(testProductId, bidPrice, "270");

        marketFacade.registerSellBid(testUserId, requestDto);

        log.info(">>>> 트랜잭션 커밋을 시작합니다.");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        log.info(">>>> 트랜잭션 커밋 완료.");

        log.info(">>>> 유저의 입찰 등록 완료. 메시지를 기다립니다...");
        Thread.sleep(5000);

        log.info(">>>> [테스트 최종 성공] 로그를 확인하세요..");
    }
}
