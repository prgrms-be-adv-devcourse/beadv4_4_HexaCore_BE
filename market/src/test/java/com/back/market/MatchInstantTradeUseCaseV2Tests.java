package com.back.market;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.app.usecase.MatchInstantTradeUseCase;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.domain.enums.OrderStatus;
import com.back.market.domain.enums.Role;
import com.back.market.dto.enums.PayAndHoldStatus;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.PayAndHoldResponseDto;
import com.back.market.mapper.BiddingMapper;
import com.back.market.mapper.MarketProductMapper;
import com.back.market.mapper.MarketUserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class MatchInstantTradeUseCaseV2Tests {
    // ... 필드 주입 부분은 동일 ...
    @Autowired private MatchInstantTradeUseCase matchInstantTradeUseCase;
    @Autowired private BiddingRepository biddingRepository;
    @Autowired private MarketUserRepository marketUserRepository;
    @Autowired private MarketProductRepository marketProductRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired private BiddingMapper biddingMapper;
    @Autowired private MarketUserMapper marketUserMapper;
    @Autowired private MarketProductMapper marketProductMapper;

    @Test
    @DisplayName("정합성 검사: 요청 시점의 가격(화면 가격)과 실제 최저가가 다르면 예외가 발생해야 한다")
    void buyNow_fail_price_mismatch() {
        // [Given] 실제 최저가는 20만원
        Long productId = 700L;
        Long seller = 13L;
        Long buyer = 14L;
        setupBaseData(productId, seller, buyer, "주소");
        createBidding(productId, seller, 200000, BiddingPosition.SELL);

        // [When] 사용자가 화면에서 19만원을 보고 요청했다고 가정 (가격 불일치)
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(190000), "270");

        // [Then] 예외 발생 (Bad Request or Price Mismatch)
        // 주의: UseCase에 이 검증 로직(request.price != bidding.price)이 구현되어 있어야 통과합니다.
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            matchInstantTradeUseCase.buyNow(buyer, request);
        });

        assertThat(exception.getMessage()).isEqualTo(FailureCode.AMOUNT_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("FIFO 검증: 동일한 가격의 판매 입찰이 여러 개일 경우, 가장 먼저 등록된(오래된) 입찰이 체결되어야 한다")
    void buyNow_match_oldest_bidding_first() {
        // [Given]
        Long productId = 600L;
        Long seller1 = 10L;
        Long seller2 = 11L;
        Long buyer = 12L;
        setupBaseData(productId, seller1, buyer, "주소");
        marketUserRepository.save(MarketUser.builder().id(seller2).nickname("s2").email("s2@t.com").role(Role.USER).build());

        // 동일 가격(20만원)으로 시간차를 두고 입찰 등록
        createBidding(productId, seller1, 200000, BiddingPosition.SELL); // 1. 먼저 등록 (Target)
        try { Thread.sleep(100); } catch (InterruptedException e) {} // 시간차 확보
        createBidding(productId, seller2, 200000, BiddingPosition.SELL); // 2. 나중에 등록

        // [When] 구매 실행
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");
        PayAndHoldResponseDto response = matchInstantTradeUseCase.buyNow(buyer, request);

        // [Then] seller1(먼저 등록한 사람)의 입찰과 체결되었는지 확인
        Order savedOrder = orderRepository.findById(response.relId()).orElseThrow();
        Long matchedSellerId = savedOrder.getSellBidding().getMarketUser().getId();

        assertThat(matchedSellerId).isEqualTo(seller1); // seller2가 되면 실패!
    }

    @Test
    @DisplayName("즉시 판매 실패: 본인이 등록한 구매 입찰에 즉시 판매(자전거래)하려 하면 예외가 발생한다")
    void sellNow_fail_selfTrading() {
        // [Given] 유저와 상품 세팅
        Long productId = 500L; // 기존 테스트와 겹치지 않게 ID 설정
        Long userId = 1L;      // 구매자이자 동시에 판매자가 될 유저
        setupBaseData(productId, userId, userId, "서울시 마포구");

        // [Given] 본인이 "15만원에 사겠다(BUY)"고 입찰 등록
        createBidding(productId, userId, 150000, BiddingPosition.BUY);

        // [When] 본인이 "15만원에 팔겠다(즉시 판매)"고 요청
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(150000), "270");

        // [Then] SELF_TRADING_NOT_ALLOWED 예외 발생 검증
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            matchInstantTradeUseCase.sellNow(userId, request);
        });

        assertThat(exception.getMessage()).isEqualTo(FailureCode.SELF_TRADING_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("통합 검증: 즉시 구매 성공 시 결제 모듈이 호출되고 주문(PAID)이 최종 저장된다")
    void buyNow_integration_success() {
        // [Given]
        Long productId = 100L;
        Long sellerId = 1L;
        Long buyerId = 2L;
        setupBaseData(productId, sellerId, buyerId, "서울시 강남구");
        createBidding(productId, sellerId, 200000, BiddingPosition.SELL);

        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");

        // [When] UseCase 호출 (반환값 DTO로 변경)
        PayAndHoldResponseDto response = matchInstantTradeUseCase.buyNow(buyerId, request);

        // [Then] 1. 응답 상태 검증 (PAID)
        assertThat(response.status()).isEqualTo(PayAndHoldStatus.PAID);
        assertThat(response.walletUsedAmount()).isEqualByComparingTo(BigDecimal.valueOf(200000));

        // [Then] 2. 주문 저장 확인
        Order savedOrder = orderRepository.findById(response.relId()).orElseThrow();
        assertThat(savedOrder.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID); // 상태 확인

        // 3. 입찰 상태 변경 확인
        Bidding buyBid = savedOrder.getBuyBidding();
        assertThat(buyBid.getStatus()).isEqualTo(BiddingStatus.MATCHED);
    }

    @Test
    @DisplayName("즉시 구매 실패: 해당 상품에 판매 입찰(SELL)이 하나도 없으면 예외가 발생한다")
    void buyNow_fail_noBidding() {
        // ... (예외 테스트는 수정할 필요 없음)
        Long productId = 300L;
        Long buyerId = 5L;
        setupBaseData(productId, 999L, buyerId, "서울");

        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            matchInstantTradeUseCase.buyNow(buyerId, request);
        });

        assertThat(exception.getMessage()).isEqualTo(FailureCode.BIDDING_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("즉시 거래 실패: 본인이 등록한 입찰과 체결(자전거래)하려 하면 예외가 발생한다")
    void executeTrade_fail_selfTrading() {
        // ... (예외 테스트는 수정할 필요 없음)
        Long productId = 400L;
        Long userId = 1L;
        setupBaseData(productId, userId, userId, "주소");
        createBidding(productId, userId, 200000, BiddingPosition.SELL);

        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            matchInstantTradeUseCase.buyNow(userId, request);
        });

        assertThat(exception.getMessage()).isEqualTo(FailureCode.SELF_TRADING_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("즉시 구매 성공(PG필요): 예치금이 부족하면 REQUIRES_PG 상태를 반환한다")
    void buyNow_success_requiresPg() {
        // [Given] 예치금 부족 상황을 가정하는 금액 (9000)
        Long productId = 100L;
        Long sellerId = 1L;
        Long buyerId = 2L;
        String buyerAddress = "서울시 강남구 역삼동";
        setupBaseData(productId, sellerId, buyerId, buyerAddress);

        // 판매 입찰 (9000)
        createBidding(productId, sellerId, 9000, BiddingPosition.SELL);

        // [When] 구매 실행
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(9000), "270");
        PayAndHoldResponseDto response = matchInstantTradeUseCase.buyNow(buyerId, request);

        // [Then] 응답 검증
        assertThat(response.status()).isEqualTo(PayAndHoldStatus.REQUIRES_PG);
        assertThat(response.tossOrderId()).isNotNull();

        // [Then] 주문 상태 확인 (결제 대기이므로 HOLD 상태여야 함 - 만약 로직에서 건드리지 않았다면)
        Order savedOrder = orderRepository.findById(response.relId()).orElseThrow();
        // NOTE: 로직 구현에 따라 HOLD(기본값) 또는 PENDING_PAYMENT 등일 수 있음
        assertThat(savedOrder.getOrderStatus()).isNotEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("즉시 판매 성공: 판매자는 결제 없이 즉시 PAID 처리된다")
    void sellNow_success() {
        // [Given]
        Long productId = 200L;
        Long buyerId = 4L;
        Long sellerId = 3L;
        String buyerAddress = "경기도 성남시 분당구";
        setupBaseData(productId, sellerId, buyerId, buyerAddress);

        createBidding(productId, buyerId, 150000, BiddingPosition.BUY);

        // [When] 즉시 판매 (반환값 변경)
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(150000), "270");
        PayAndHoldResponseDto response = matchInstantTradeUseCase.sellNow(sellerId, request);

        // [Then] 1. 응답 상태 검증 (PAID)
        assertThat(response.status()).isEqualTo(PayAndHoldStatus.PAID);

        // [Then] 2. 주문 정보 검증
        Order savedOrder = orderRepository.findById(response.relId()).orElseThrow();
        assertThat(savedOrder.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        assertThat(savedOrder.getAddress()).isEqualTo(buyerAddress);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID); // 즉시 결제 완료

        // [Then] 3. 입찰 상태 검증
        assertThat(savedOrder.getSellBidding().getStatus()).isEqualTo(BiddingStatus.MATCHED);
    }

    // --- Helper Methods (기존과 동일) ---
    private void setupBaseData(Long productId, Long sellerId, Long buyerId, String buyerAddress) {
        // ... (기존 코드 그대로)
        MarketUser seller = marketUserMapper.toEntity(sellerId, Role.USER, "seller", "s@t.com", "판매자주소", "010-1234-5678", "img");
        marketUserRepository.save(seller);
        MarketUser buyer = marketUserMapper.toEntity(buyerId, Role.USER, "buyer", "b@t.com", buyerAddress, "010-1234-5678", "img");
        marketUserRepository.save(buyer);
        if (!marketProductRepository.existsById(productId)) {
            marketProductRepository.save(marketProductMapper.toEntity(productId, "N", "신발", "N1", "270", 100000L, "S", "img"));
        }
    }

    private Bidding createBidding(Long productId, Long userId, long price, BiddingPosition position) {
        // ... (기존 코드 그대로)
        MarketProduct product = marketProductRepository.findById(productId).orElseThrow();
        MarketUser user = marketUserRepository.findById(userId).orElseThrow();
        BiddingRequestDto requestDto = BiddingRequestDto.of(productId, BigDecimal.valueOf(price), "270");
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, position);

        bidding.changeStatus(BiddingStatus.PROCESS); //테스트 데이터이고, 매칭 대상이 되어야 하므로 PROCESS로 변경

        return biddingRepository.save(bidding);
    }
}