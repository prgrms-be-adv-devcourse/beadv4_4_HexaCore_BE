package com.back.market.event;

import com.back.common.dto.cash.enums.PayAndHoldStatus;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.adapter.out.cash.MarketCashAdapter;
import com.back.market.app.MarketFacade;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.dto.request.BiddingRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class NotificationEventPublishTests {

    @Autowired
    private MarketFacade marketFacade;
    @Autowired
    private MarketUserRepository userRepository;
    @Autowired
    private BiddingRepository biddingRepository;
    @Autowired
    private MarketProductRepository productRepository;
    @MockitoBean
    private MarketCashAdapter marketCashAdapter;

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("판매 입찰 등록 시 카프카로 알림 이벤트가 발행되는지 확인")
    void registerSellBid_PublishKafkaMessage() throws InterruptedException {
        Long testUserId = 999L;
        Long testProductId = 200L;

        userRepository.save(MarketUser.builder()
                .id(testUserId)
                .name("테스트판매자")
                .email("test-seller@example.com")
                .build());

        BigDecimal bidPrice = new BigDecimal("1000000"); // 100만원
        BiddingRequestDto requestDto = BiddingRequestDto.of(testProductId, bidPrice, "270");

        marketFacade.registerSellBid(testUserId, "127.0.0.1", requestDto);

        log.info(">>>> 트랜잭션 커밋을 시작합니다.");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        log.info(">>>> 트랜잭션 커밋 완료.");

        log.info(">>>> 유저의 입찰 등록 완료. 메시지를 기다립니다...");
        Thread.sleep(5000);

        log.info(">>>> [테스트 최종 성공] 로그를 확인하세요..");
    }

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("즉시 구매 체결 시 카프카로 주문 생성 이벤트(OrderCreated)가 발행되는지 확인")
    public void buyNow_PublishKafkaMessage() throws InterruptedException {
        Long sellerId = 999L;
        Long buyerId = 998L;
        Long productId = 999L;

        MarketUser seller = userRepository.save(MarketUser.builder()
                .id(sellerId)
                .name("판매자")
                .email("seller@test.com")
                .phone("010-1234-5678") // 테스트 통과를 위한 더미 연락처
                .address("서울시 강남구 역삼동") // 테스트 통과를 위한 더미 주소
                .build());

        MarketUser buyer = userRepository.save(MarketUser.builder()
                .id(buyerId)
                .name("구매자")
                .email("buyer@test.com")
                .phone("010-9876-5432") // 테스트 통과를 위한 더미 연락처
                .address("서울시 서초구 서초동") // 테스트 통과를 위한 더미 주소
                .build());

        MarketProduct testProduct = productRepository.save(MarketProduct.builder()
                .id(productId)
                .name("테스트용 신발")
                .brandName("TestBrand")
                .categoryName("Sneakers")
                .productNumber("TEST-001")
                .productOption("270")
                .releasePrice(new BigDecimal("100000"))
                .thumbnailImage("test_image_url")
                .build());

        // 2. 매칭용 판매 입찰(Bidding) 더미 데이터 생성
        BigDecimal matchPrice = new BigDecimal("500000");
        biddingRepository.save(Bidding.builder()
                .marketUser(seller)
                .marketProduct(productRepository.findById(productId).orElseThrow())
                .price(matchPrice)
                .position(BiddingPosition.SELL)
                .status(BiddingStatus.PROCESS)
                .build());

        // 구매 요청 DTO
        BiddingRequestDto requestDto = BiddingRequestDto.of(productId, matchPrice, "270");

        // When: 즉시 구매 실행
        log.info(">>>> 즉시 구매 시작");
        given(marketCashAdapter.getPayAndHoldResult(any()))
                .willReturn(PayAndHoldResponseDto.of(
                        PayAndHoldStatus.PAID,
                        RelType.ORDER,
                        1L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null
                ));
        marketFacade.purchaseNow(buyerId, "127.0.0.1", requestDto);

        // Then: 트랜잭션 종료 후 카프카 발행 로그 확인
        log.info(">>>> 트랜잭션 커밋 시작");
        TestTransaction.flagForCommit();
        TestTransaction.end();

        log.info(">>>> 5초 대기하며 로그를 확인하세요...");
        Thread.sleep(5000);
    }

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("즉시 판매 체결 시 카프카로 주문 생성 이벤트가 발행되는지 확인")
    void sellNow_PublishKafkaMessage() throws InterruptedException {
        // 1. Given: 신규 유저 및 신규 상품 준비
        Long sellerId = 888L;
        Long buyerId = 887L;
        Long productId = 888L;

        // 판매자/구매자 생성 (전화번호, 주소 필수)
        MarketUser seller = userRepository.save(MarketUser.builder()
                .id(sellerId).name("즉시판매자").email("seller888@test.com")
                .phone("010-1111-8888").address("서울").build());
        MarketUser buyer = userRepository.save(MarketUser.builder()
                .id(buyerId).name("즉시구매자").email("buyer887@test.com")
                .phone("010-2222-8887").address("경기").build());

        // 상품 생성
        MarketProduct product = productRepository.save(MarketProduct.builder()
                .id(productId).name("판매테스트신발").brandName("Nike").categoryName("Sneakers")
                .productNumber("SELL-TEST-888").productOption("260").releasePrice(new BigDecimal("150000"))
                .thumbnailImage("image_url").build());

        // 미리 등록된 '구매 입찰'이 있어야 즉시 판매가 가능함
        BigDecimal matchPrice = new BigDecimal("200000");
        biddingRepository.save(Bidding.builder()
                .marketUser(buyer)
                .marketProduct(product)
                .price(matchPrice)
                .position(BiddingPosition.BUY) // 이번엔 BUY 입찰을 먼저 생성
                .status(BiddingStatus.PROCESS)
                .build());

        // Cash 서비스 응답 Mocking (에러 방지)
        given(marketCashAdapter.getPayAndHoldResult(any()))
                .willReturn(PayAndHoldResponseDto.of(
                        PayAndHoldStatus.PAID, RelType.ORDER, 1L, BigDecimal.ZERO, BigDecimal.ZERO, null));

        // 즉시 판매 요청 DTO
        BiddingRequestDto requestDto = BiddingRequestDto.of(productId, matchPrice, "260");

        // 2. When: 즉시 판매 실행
        log.info(">>>> 즉시 판매 시작 (판매자 ID: {})", sellerId);
        marketFacade.sellNow(sellerId, "127.0.0.1", requestDto);

        // 3. Then: 트랜잭션 종료 및 카프카 로그 확인
        log.info(">>>> 트랜잭션 커밋 시작");
        TestTransaction.flagForCommit();
        TestTransaction.end();

        log.info(">>>> 5초 대기하며 [OrderCreatedPayload] 로그를 확인하세요 (biddingPosition=SELL 예상)");
        Thread.sleep(5000);
    }
}
