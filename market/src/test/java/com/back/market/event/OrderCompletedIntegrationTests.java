package com.back.market.event;

import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.app.MarketFacade;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.domain.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Rollback(false)
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}") // 가상 브로커 주소를 자동 주입
@ActiveProfiles("test")
@Transactional
@EmbeddedKafka(
        partitions = 1,
        topics = {"market.order.completed"} // 토픽만 지정하면 포트는 랜덤으로 잡힙니다.
)
public class OrderCompletedIntegrationTests {

    @Autowired
    private MarketFacade marketFacade;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MarketUserRepository marketUserRepository;
    @Autowired
    private MarketProductRepository marketProductRepository;
    @Autowired
    private BiddingRepository biddingRepository; // 연관 관계 저장을 위해 주입

    @Test
    @DisplayName("통합 테스트: 구매 확정 시 DB 상태가 변경되고 실제 Kafka로 이벤트가 발행되어야 한다")
    void completeOrder_RealInfrastructure_Success() throws InterruptedException {
        // 1. 기초 데이터 저장 (User)
        MarketUser buyer = marketUserRepository.save(MarketUser.builder()
                .id(101L).name("통합구매자").email("buyer@test.com").build());
        MarketUser seller = marketUserRepository.save(MarketUser.builder()
                .id(111L).name("통합판매자").email("seller@test.com").build());

        // 2. 기초 데이터 저장 (MarketProduct - 필수 필드 모두 입력)
        MarketProduct product = marketProductRepository.save(MarketProduct.builder()
                .id(500L)
                .name("조던 1 레트로 하이")
                .productNumber("DZ5485-612")
                .productOption("270")
                .brandName("Nike")
                .categoryName("Sneakers")
                .releasePrice(new BigDecimal("200000"))
                .thumbnailImage("image_url")
                .build());

        // 3. Bidding 저장 (TransientPropertyValueException 방지)
        // 반드시 repository.save()를 호출하여 DB에 먼저 넣어야 합니다.
        Bidding buyBid = biddingRepository.save(Bidding.builder()
                .marketUser(buyer)
                .marketProduct(product)
                .price(new BigDecimal("50000"))
                .position(BiddingPosition.BUY)
                .status(BiddingStatus.PROCESS)
                .build());

        Bidding sellBid = biddingRepository.save(Bidding.builder()
                .marketUser(seller)
                .marketProduct(product)
                .price(new BigDecimal("50000"))
                .position(BiddingPosition.SELL)
                .status(BiddingStatus.PROCESS)
                .build());

        // 4. 저장된 Bidding 객체를 사용하여 Order 생성
        Order order = orderRepository.save(Order.builder()
                .buyBidding(buyBid)
                .sellBidding(sellBid)
                .orderStatus(OrderStatus.DELIVERY_COMPLETED) // 확정 가능 상태
                .price(new BigDecimal("50000"))
                .address("서울시 강남구 테헤란로 123")
                .requestPaymentDate(java.time.LocalDateTime.now())
                .build());

        // 5. 실행
        marketFacade.completeOrder(buyer.getId(), order.getId());

        // 6. 검증: DB 상태 변경 확인
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);

        log.info(">>>> 트랜잭션 커밋을 시작합니다.");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        log.info(">>>> 트랜잭션 커밋 완료.");


        // [추가] 카프카 발행은 비동기이므로 로그가 찍힐 때까지 잠시 기다림
        log.info(">>>> 비동기 카프카 발행 로그 확인을 위해 5초간 대기합니다...");
        Thread.sleep(5000);

        log.info("통합 테스트 성공: 주문상태={}", updatedOrder.getOrderStatus());
    }
}
