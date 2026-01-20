package com.back.market;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.app.usecase.RegisterBidUseCase;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.domain.enums.Role;
import com.back.market.dto.enums.PayAndHoldStatus;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.PayAndHoldResponseDto;
import org.junit.jupiter.api.BeforeEach;
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
public class RegisterBidUseCaseTestsV2 {
    @Autowired RegisterBidUseCase useCase;
    @Autowired BiddingRepository biddingRepository;
    @Autowired MarketProductRepository productRepository;
    @Autowired MarketUserRepository userRepository;

    // 테스트마다 공통으로 사용할 ID 상수를 정의하면 편리합니다.
    private final Long USER_ID = 10L;
    private final Long PRODUCT_ID = 100L;

    @BeforeEach
    void setUp() {
        // [Given] 기초 데이터 세팅 (기존 코드 스타일 활용)
        if (!userRepository.existsById(USER_ID)) {
            MarketUser user = MarketUser.builder()
                    .id(USER_ID)
                    .nickname("tester")
                    .email("test@test.com")
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
        }

        if (!productRepository.existsById(PRODUCT_ID)) {
            MarketProduct product = MarketProduct.builder()
                    .id(PRODUCT_ID)
                    .name("나이키 신발")
                    .productNumber("NK-123")
                    .productOption("270")
                    .releasePrice(BigDecimal.valueOf(100000))
                    .build();
            productRepository.save(product);
        }
    }

    @Test
    @DisplayName("구매 입찰(성공): 예치금이 충분하면 PAID 상태가 반환되고 입찰은 PROCESS 상태가 된다")
    void registerBuyBid_success() {
        // [Given] 100,000원 (FakeCashClient에서 정상 결제 처리)
        BigDecimal price = BigDecimal.valueOf(100000);
        BiddingRequestDto request = BiddingRequestDto.of(PRODUCT_ID, price, "270");

        // [When] DTO 반환 확인
        PayAndHoldResponseDto response = useCase.registerBuyBid(USER_ID, request);

        // [Then] 1. 응답 검증
        assertThat(response.status()).isEqualTo(PayAndHoldStatus.PAID);
        assertThat(response.walletUsedAmount()).isEqualByComparingTo(price);

        // [Then] 2. 저장된 입찰 상태 검증
        Bidding savedBidding = biddingRepository.findById(response.relId()).orElseThrow();
        assertThat(savedBidding.getPosition()).isEqualTo(BiddingPosition.BUY);
        assertThat(savedBidding.getStatus()).isEqualTo(BiddingStatus.PROCESS); // 활성화 확인
    }

    @Test
    @DisplayName("구매 입찰(PG필요): 예치금 부족(9999원) 시 예외가 아닌 REQUIRES_PG 상태를 반환해야 한다")
    void registerBuyBid_requiresPg() {
        // [Given] 9,999원 (FakeCashClient에서 PG 필요로 처리)
        BigDecimal price = BigDecimal.valueOf(9000);
        BiddingRequestDto request = BiddingRequestDto.of(PRODUCT_ID, price, "270");

        // [When] 예외가 발생하지 않고 응답을 받아야 함
        PayAndHoldResponseDto response = useCase.registerBuyBid(USER_ID, request);

        // [Then]
        assertThat(response.status()).isEqualTo(PayAndHoldStatus.REQUIRES_PG);
        assertThat(response.pgRequiredAmount()).isEqualByComparingTo(price);
        assertThat(response.tossOrderId()).isNotNull(); // 토스 ID 존재 확인

        // 입찰은 생성되어 있어야 함 (나중에 PG 완료 시 PROCESS로 변경됨)
        Bidding savedBidding = biddingRepository.findById(response.relId()).orElseThrow();
        assertThat(savedBidding).isNotNull();
    }

    @Test
    @DisplayName("판매 입찰(성공): 결제 없이 항상 PAID 상태를 반환해야 한다")
    void registerSellBid_success() {
        // [Given]
        BiddingRequestDto request = BiddingRequestDto.of(PRODUCT_ID, BigDecimal.valueOf(150000), "270");

        // [When]
        PayAndHoldResponseDto response = useCase.registerSellBid(USER_ID, request);

        // [Then]
        assertThat(response.status()).isEqualTo(PayAndHoldStatus.PAID);
        assertThat(response.walletUsedAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        Bidding savedBidding = biddingRepository.findById(response.relId()).orElseThrow();
        assertThat(savedBidding.getPosition()).isEqualTo(BiddingPosition.SELL);
    }

    @Test
    @DisplayName("구매 입찰: 가격 단위가 1000원 단위가 아니면 실패해야 한다")
    void registerBuyBid_fail_invalid_unit() {
        // [Given]
        BigDecimal invalidPrice = BigDecimal.valueOf(30500);
        BiddingRequestDto request = BiddingRequestDto.of(PRODUCT_ID, invalidPrice, "270");

        // [When & Then]
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            useCase.registerBuyBid(USER_ID, request);
        });
        assertThat(exception.getMessage()).isEqualTo(FailureCode.INVALID_PRICE_UNIT.getMessage());
    }

    @Test
    @DisplayName("구매 입찰: 이미 더 저렴한 판매 입찰이 존재하면 '즉시 구매'를 유도하며 실패해야 한다")
    void registerBuyBid_fail_induce_immediate_purchase() {
        // [Given] 다른 판매자 생성
        Long sellerId = 20L;
        MarketUser seller = MarketUser.builder()
                .id(sellerId)
                .nickname("seller_user")
                .email("seller@test.com")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        // 판매 입찰 등록 (가격 30,000원)
        MarketProduct product = productRepository.findById(PRODUCT_ID).orElseThrow();
        Bidding existingSellBid = Bidding.builder()
                .marketUser(seller)
                .marketProduct(product)
                .position(BiddingPosition.SELL)
                .status(BiddingStatus.PROCESS)
                .price(BigDecimal.valueOf(30000))
                .build();
        biddingRepository.save(existingSellBid);

        // [When] 내가 30,000원에 사겠다고 구매 입찰 시도 (가격이 같거나 높으므로 즉시 구매 유도 에러)
        BiddingRequestDto request = BiddingRequestDto.of(PRODUCT_ID, BigDecimal.valueOf(30000), "270");

        // [Then]
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            useCase.registerBuyBid(USER_ID, request);
        });
        assertThat(exception.getMessage()).isEqualTo(FailureCode.INVALID_BID_PRICE_BUY.getMessage());
    }
}
