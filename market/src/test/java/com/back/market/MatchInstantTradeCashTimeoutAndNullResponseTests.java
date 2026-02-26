package com.back.market;

import com.back.common.response.CommonResponse;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.adapter.out.cash.CashClient;
import com.back.market.app.usecase.MatchInstantTradeUseCase;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.mapper.BiddingMapper;
import com.back.market.mapper.MarketProductMapper;
import com.back.market.mapper.MarketUserMapper;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.common.code.SuccessCode;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class MatchInstantTradeCashTimeoutAndNullResponseTests {

    private static final Logger log = LoggerFactory.getLogger(MatchInstantTradeCashTimeoutAndNullResponseTests.class);

    @Autowired
    private MatchInstantTradeUseCase matchInstantTradeUseCase;
    @Autowired private BiddingRepository biddingRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private BiddingMapper biddingMapper;
    @Autowired private MarketProductMapper marketProductMapper;
    @Autowired private MarketUserMapper marketUserMapper;
    @Autowired private MarketUserRepository marketUserRepository;
    @Autowired private MarketProductRepository marketProductRepository;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public CashClient cashClient() {
            return new CashClient() {
                @Override
                public CommonResponse<PayAndHoldResponseDto> requestBidHold(PayAndHoldRequestDto requestDto) {
                    // 시뮬레이션: null 응답 처리 테스트
                    return null;
                }

                @Override
                public CommonResponse<PaymentCancelResponseDto> refundBidHold(com.back.common.dto.cash.request.PaymentCancelRequestDto requestDto) {
                    // 보상에서 호출될 수 있으므로 정상 응답을 반환
                    return CommonResponse.success(SuccessCode.OK, new PaymentCancelResponseDto(requestDto.userId(), java.math.BigDecimal.ZERO));
                }
            };
        }
    }

    @Test
    @DisplayName("Cash 모듈이 null 응답을 반환하면 예외가 발생하고 REQUIRES_NEW 보상으로 판매 입찰이 PROCESS로 복구되어야 한다")
    void cash_null_response_triggers_compensation() {
        // Given
        Long productId = 888L;
        Long sellerId = 77L;
        Long buyerId = 78L;
        setupBaseData(productId, sellerId, buyerId, "서울");
        Bidding sellBid = createBidding(productId, sellerId, 7000, BiddingPosition.SELL);

        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(7000), "270");

        // When
        Exception ex = assertThrows(Exception.class, () -> {
            matchInstantTradeUseCase.buyNow(buyerId, request);
        });

        // 로그 출력: 예외 및 인자
        log.error("Test: caught exception during buyNow: {}", ex.toString(), ex);

        // Then: 입찰 상태가 PROCESS로 복구되어야 함
        Bidding found = biddingRepository.findById(sellBid.getId()).orElseThrow();
        log.info("Test: sellBid id={}, status after compensation={}", sellBid.getId(), found.getStatus());
        assertThat(found.getStatus()).isEqualTo(BiddingStatus.PROCESS);
    }

    // Helper methods
    private void setupBaseData(Long productId, Long sellerId, Long buyerId, String buyerAddress) {
        MarketUser seller = marketUserMapper.toEntity(sellerId,"seller", "s@t.com", "판매자주소", "010-1234-5678");
        marketUserRepository.save(seller);

        MarketUser buyer = marketUserMapper.toEntity(buyerId,"buyer", "b@t.com", buyerAddress, "010-1234-5678");
        marketUserRepository.save(buyer);

        if (!marketProductRepository.existsById(productId)) {
            marketProductRepository.save(marketProductMapper.toEntity(
                    productId,
                    1L,           // productInfoId (원본 정보 ID - 테스트용 임의 값)
                    "Nike",       // brandName
                    "신발",        // name
                    "N1",         // productNumber
                    "270",        // productOption (사이즈)
                    BigDecimal.valueOf(100000L),      // price
                    "카테고리",     // categoryName
                    "img"
            ));
        }
    }

    private Bidding createBidding(Long productId, Long userId, long price, BiddingPosition position) {
        MarketProduct product = marketProductRepository.findById(productId).orElseThrow();
        MarketUser user = marketUserRepository.findById(userId).orElseThrow();
        BiddingRequestDto requestDto = BiddingRequestDto.of(productId, BigDecimal.valueOf(price), "270");
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, position);
        bidding.changeStatus(BiddingStatus.PROCESS);
        return biddingRepository.save(bidding);
    }
}

