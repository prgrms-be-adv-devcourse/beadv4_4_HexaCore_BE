package com.back.market;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.app.usecase.GetInstantPriceUseCase;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.Role;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import com.back.market.mapper.BiddingMapper;
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
class GetInstantPriceUseCaseTests {

    @Autowired
    private GetInstantPriceUseCase getInstantPriceUseCase;

    @Autowired private BiddingRepository biddingRepository;
    @Autowired private MarketUserRepository marketUserRepository;
    @Autowired private MarketProductRepository marketProductRepository;
    @Autowired private BiddingMapper biddingMapper;

    @Test
    @DisplayName("조회 실패: 존재하지 않는 상품 ID로 즉시구매가를 조회하면 PRODUCT_NOT_FOUND 예외가 발생해야 한다")
    void getBuyNowPrice_fail_productNotFound() {
        // [Given] 존재하지 않는 상품 ID
        Long invalidProductId = 9999L;

        // [When & Then] 예외 발생 확인 (RegisterBidUseCaseTests와 동일한 방식)
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            getInstantPriceUseCase.getBuyNowPrice(invalidProductId);
        });

        // 에러 코드 검증
        assertThat(exception.getMessage()).isEqualTo(FailureCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("즉시 구매가 조회: 판매 입찰(SELL) 중 가장 낮은 가격을 반환해야 한다")
    void getBuyNowPrice_Success() {
        // [Given] 기초 데이터 및 입찰 데이터 세팅
        Long productId = 100L;
        setupBaseData(productId, 1L);

        // 판매 입찰 3개 등록 (20만, 18만, 22만)
        createBidding(productId, 1L, 200000, BiddingPosition.SELL);
        createBidding(productId, 1L, 180000, BiddingPosition.SELL); // 최저가 (정답)
        createBidding(productId, 1L, 220000, BiddingPosition.SELL);

        // [When] UseCase 메서드 직접 호출
        InstantBuyPriceResponseDto result = getInstantPriceUseCase.getBuyNowPrice(productId);

        // [Then]
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.buyNowPrice()).isEqualByComparingTo("180000"); // 18만원 확인
    }

    @Test
    @DisplayName("즉시 판매가 조회: 구매 입찰(BUY) 중 가장 높은 가격을 반환해야 한다")
    void getSellNowPrice_Success() {
        // [Given] 기초 데이터 및 입찰 데이터 세팅
        Long productId = 100L;
        setupBaseData(productId, 2L);

        // 구매 입찰 3개 등록 (15만, 16만, 14만)
        createBidding(productId, 2L, 150000, BiddingPosition.BUY);
        createBidding(productId, 2L, 160000, BiddingPosition.BUY); // 최고가 (정답)
        createBidding(productId, 2L, 140000, BiddingPosition.BUY);

        // [When] UseCase 메서드 직접 호출
        InstantSellPriceResponseDto result = getInstantPriceUseCase.getSellNowPrice(productId);

        // [Then]
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.sellNowPrice()).isEqualByComparingTo("160000"); // 16만원 확인
    }

    @Test
    @DisplayName("입찰 내역이 없는 경우: 결과값(Price)이 null로 반환되어야 한다")
    void getPrice_ReturnNull_WhenNoBiddings() {
        // [Given] 상품은 존재하지만 입찰 내역은 없는 상태
        Long productId = 200L;
        setupBaseData(productId, 1L);

        // [When]
        InstantBuyPriceResponseDto buyResult = getInstantPriceUseCase.getBuyNowPrice(productId);
        InstantSellPriceResponseDto sellResult = getInstantPriceUseCase.getSellNowPrice(productId);

        // [Then]
        assertThat(buyResult.buyNowPrice()).isNull();
        assertThat(sellResult.sellNowPrice()).isNull();
    }

    // --- Helper Methods (RegisterBidUseCaseTests 스타일과 통일) ---

    private void setupBaseData(Long productId, Long userId) {
        // 유저 생성
        if (!marketUserRepository.existsById(userId)) {
            marketUserRepository.save(MarketUser.builder()
                    .id(userId)
                    .nickname("tester")
                    .email("test@test.com")
                    .role(Role.USER)
                    .build());
        }
        // 상품 생성
        if (!marketProductRepository.existsById(productId)) {
            marketProductRepository.save(MarketProduct.builder()
                    .id(productId)
                    .name("테스트 신발")
                    .productNumber("TEST-001")
                    .productOption("270")
                    .brandName("TestBrand")
                    .categoryName("Sneakers")
                    .releasePrice(BigDecimal.valueOf(100000))
                    .build());
        }
    }

    private void createBidding(Long productId, Long userId, long price, BiddingPosition position) {
        MarketProduct product = marketProductRepository.findById(productId).orElseThrow();
        MarketUser user = marketUserRepository.findById(userId).orElseThrow();

        BiddingRequestDto requestDto = BiddingRequestDto.of(productId, BigDecimal.valueOf(price), "270");

        // BiddingMapper를 사용하여 엔티티 생성 및 저장
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, position);
        biddingRepository.save(bidding);
    }
}