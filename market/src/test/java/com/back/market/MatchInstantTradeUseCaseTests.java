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
import com.back.market.dto.request.BiddingRequestDto;
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
public class MatchInstantTradeUseCaseTests {
    @Autowired private MatchInstantTradeUseCase matchInstantTradeUseCase;
    @Autowired private BiddingRepository biddingRepository;
    @Autowired private MarketUserRepository marketUserRepository;
    @Autowired private MarketProductRepository marketProductRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private BiddingMapper biddingMapper;
    @Autowired private MarketUserMapper marketUserMapper;
    @Autowired private MarketProductMapper marketProductMapper;

    @Test
    @DisplayName("통합 검증: 즉시 구매 성공 시 결제 모듈(Fake)이 호출되고 주문이 최종 저장된다")
    void buyNow_integration_success() {
        // [Given]
        Long productId = 100L;
        Long sellerId = 1L;
        Long buyerId = 2L;
        setupBaseData(productId, sellerId, buyerId, "서울시 강남구");
        createBidding(productId, sellerId, 200000, BiddingPosition.SELL);

        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");

        // [When] 실제 UseCase 호출 (내부에서 FakeCashClient의 "요청 성공" 로직이 실행됨)
        Long orderId = matchInstantTradeUseCase.buyNow(buyerId, request);

        // [Then] 1. 주문 저장 확인 ✅
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(savedOrder.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));

        // 2. 입찰 상태 변경 확인 ✅
        Bidding buyBid = savedOrder.getBuyBidding();
        assertThat(buyBid.getStatus()).isEqualTo(BiddingStatus.MATCHED);
    }

    @Test
    @DisplayName("즉시 구매 실패: 해당 상품에 판매 입찰(SELL)이 하나도 없으면 예외가 발생한다")
    void buyNow_fail_noBidding() {
        // [Given] 상품은 존재하지만 판매 입찰은 없는 상태
        Long productId = 300L;
        Long buyerId = 5L;
        setupBaseData(productId, 999L, buyerId, "서울");

        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");

        // [When & Then] BIDDING_NOT_FOUND 예외 확인
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            matchInstantTradeUseCase.buyNow(buyerId, request);
        });

        assertThat(exception.getMessage()).isEqualTo(FailureCode.BIDDING_NOT_FOUND.getMessage()); //
    }

    @Test
    @DisplayName("즉시 거래 실패: 본인이 등록한 입찰과 체결(자전거래)하려 하면 예외가 발생한다")
    void executeTrade_fail_selfTrading() {
        // [Given] 1번 유저가 판매 입찰을 올림
        Long productId = 400L;
        Long userId = 1L;
        setupBaseData(productId, userId, userId, "주소");

        createBidding(productId, userId, 200000, BiddingPosition.SELL);

        // [When] 1번 유저가 본인의 매물을 즉시 구매 시도
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");

        // [Then] SELF_TRADING_NOT_ALLOWED 예외 확인
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            matchInstantTradeUseCase.buyNow(userId, request);
        });

        assertThat(exception.getMessage()).isEqualTo(FailureCode.SELF_TRADING_NOT_ALLOWED.getMessage()); //
    }

    @Test
    @DisplayName("즉시 구매 성공: 판매 입찰이 있을 때 즉시 구매하면 Bidding 상태가 변경되고 Order가 생성된다")
    void buyNow_success() {
        // [Given] 기초 데이터 세팅
        Long productId = 100L;
        Long sellerId = 1L;
        Long buyerId = 2L;
        String buyerAddress = "서울시 강남구 역삼동";

        setupBaseData(productId, sellerId, buyerId, buyerAddress);

        // 판매자가 200,000원에 판매 입찰 등록해둔 상태
        Bidding sellBid = createBidding(productId, sellerId, 200000, BiddingPosition.SELL);

        // [When] 구매자가 즉시 구매 실행 (200,000원)
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(200000), "270");
        Long orderId = matchInstantTradeUseCase.buyNow(buyerId, request);

        // [Then] 1. 주문 생성 확인
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(savedOrder.getPrice()).isEqualTo(BigDecimal.valueOf(200000));
        assertThat(savedOrder.getAddress()).isEqualTo(buyerAddress);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.HOLD);

        // [Then] 2. 입찰 상태 변경 확인 (둘 다 MATCHED)
        Bidding targetSellBid = biddingRepository.findById(sellBid.getId()).orElseThrow();
        assertThat(targetSellBid.getStatus()).isEqualTo(BiddingStatus.MATCHED);

        Bidding newBuyBid = savedOrder.getBuyBidding(); // 주문과 연결된 구매 입찰
        assertThat(newBuyBid.getStatus()).isEqualTo(BiddingStatus.MATCHED);
        assertThat(newBuyBid.getMarketUser().getId()).isEqualTo(buyerId);
    }

    @Test
    @DisplayName("즉시 판매 성공: 구매 입찰이 있을 때 즉시 판매하면 Bidding 상태가 변경되고 Order가 생성된다")
    void sellNow_success() {
        // [Given] 기초 데이터 세팅
        Long productId = 200L;
        Long buyerId = 4L;
        Long sellerId = 3L;
        String buyerAddress = "경기도 성남시 분당구";

        setupBaseData(productId, sellerId, buyerId, buyerAddress);

        // 구매자가 150,000원에 구매 입찰 등록해둔 상태
        Bidding buyBid = createBidding(productId, buyerId, 150000, BiddingPosition.BUY);

        // [When] 판매자가 즉시 판매 실행 (150,000원)
        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(150000), "270");
        Long orderId = matchInstantTradeUseCase.sellNow(sellerId, request);

        // [Then] 주문 정보 및 입찰 상태 검증
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(savedOrder.getPrice()).isEqualTo(BigDecimal.valueOf(150000));
        assertThat(savedOrder.getAddress()).isEqualTo(buyerAddress); // 구매자의 주소 확인

        assertThat(biddingRepository.findById(buyBid.getId()).get().getStatus()).isEqualTo(BiddingStatus.MATCHED);
        assertThat(savedOrder.getSellBidding().getStatus()).isEqualTo(BiddingStatus.MATCHED);
    }

    // --- Helper Methods ---

    private void setupBaseData(Long productId, Long sellerId, Long buyerId, String buyerAddress) {
        // 판매자 생성 혹은 업데이트
        MarketUser seller = marketUserMapper.toEntity(sellerId, Role.USER, "seller", "s@t.com", "판매자주소", "010-1234-5678", "https://dummyimage.com/100x100/000/fff&text=Seller");
        marketUserRepository.save(seller); // existsById 체크 없이 save(upsert) 수행

        // 구매자 생성 혹은 업데이트 (테스트에 필요한 주소로 강제 반영)
        MarketUser buyer = marketUserMapper.toEntity(buyerId, Role.USER, "buyer", "b@t.com", buyerAddress, "010-1234-5678", "https://dummyimage.com/100x100/000/fff&text=Buyer");
        marketUserRepository.save(buyer);

        // 상품 생성 혹은 업데이트
        if (!marketProductRepository.existsById(productId)) {
            marketProductRepository.save(marketProductMapper.toEntity(productId, "N", "신발", "N1", "270", 100000L, "S", "https://dummyimage.com/600x400/000/fff&text=N"));
        }
    }

    private Bidding createBidding(Long productId, Long userId, long price, BiddingPosition position) {
        MarketProduct product = marketProductRepository.findById(productId).orElseThrow();
        MarketUser user = marketUserRepository.findById(userId).orElseThrow();
        BiddingRequestDto requestDto = BiddingRequestDto.of(productId, BigDecimal.valueOf(price), "270");
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, position);
        return biddingRepository.save(bidding);
    }
}
