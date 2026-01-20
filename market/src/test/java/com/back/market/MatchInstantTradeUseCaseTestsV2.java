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
public class MatchInstantTradeUseCaseTestsV2 {
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
        return biddingRepository.save(bidding);
    }
}