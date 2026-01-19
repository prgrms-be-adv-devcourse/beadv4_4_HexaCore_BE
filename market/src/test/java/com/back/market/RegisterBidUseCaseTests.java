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
import com.back.market.dto.request.BiddingRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class RegisterBidUseCaseTests {
    @Autowired RegisterBidUseCase usecase;
    @Autowired BiddingRepository biddingRepository;
    @Autowired MarketProductRepository productRepository;
    @Autowired MarketUserRepository userRepository;

    @Test
    @DisplayName("구매 입찰: 정상적인 가격(천원 단위)과 잔액이 충분하면 등록에 성공해야 한다")
    void registerBuyBid_success() {
        // [Given] 1. 테스트용 유저 생성 (ID 수동 할당)
        Long userId = 10L; // 원본 모듈의 ID라고 가정
        MarketUser user = MarketUser.builder()
                .id(userId) // ★ 핵심: 직접 ID를 부여해야 함
                .nickname("tester")
                .email("test@test.com")
                .role(Role.USER)
                .build();
        userRepository.save(user); // H2 DB에 저장 (ID가 있으므로 persist가 아닌 merge처럼 동작하거나 저장됨)

        // [Given] 2. 테스트용 상품 생성 (ID 수동 할당)
        Long productId = 100L; // 원본 모듈의 ID라고 가정
        MarketProduct product = MarketProduct.builder()
                .id(productId) // ★ 핵심: 직접 ID를 부여해야 함
                .name("나이키 신발")
                .productNumber("NK-123")
                .productOption("270")
                .brandName("Nike")
                .categoryName("Shoes")
                .releasePrice(BigDecimal.valueOf(100000))
                .build();
        productRepository.save(product);

        // [Given] 3. 요청 객체 생성 (위에서 정한 ID 사용)
        BigDecimal price = BigDecimal.valueOf(30000);
        BiddingRequestDto request = BiddingRequestDto.of(
                productId, // 100L
                price,
                "270"
        );

        // [When]
        Long savedId = usecase.registerBuyBid(userId, request);

        // [Then]
        assertThat(savedId).isNotNull();

        Bidding savedBidding = biddingRepository.findById(savedId).orElseThrow();
        assertThat(savedBidding.getPosition()).isEqualTo(BiddingPosition.BUY);
        assertThat(savedBidding.getPrice()).isEqualByComparingTo(price);
        assertThat(savedBidding.getStatus()).isEqualTo(BiddingStatus.PROCESS);
    }

    @Test
    @DisplayName("구매 입찰: 가격 단위가 1000원 단위가 아니면 실패해야 한다")
    void registerBuyBid_fail_invalid_unit() {
        // given
        Long userId = 1L;
        BigDecimal invalidPrice = BigDecimal.valueOf(30500); // 500원 단위 (유효하지 않음)
        BiddingRequestDto request = BiddingRequestDto.of(1L, invalidPrice, "270");

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            usecase.registerBuyBid(userId, request);
        });

        // 에러 메시지가 '단위 오류'인지 확인 (선택 사항)
        assertThat(exception.getMessage()).isEqualTo(FailureCode.INVALID_PRICE_UNIT.getMessage());
    }

    @Test
    @DisplayName("구매 입찰: 이미 더 저렴한 판매 입찰이 존재하면 '즉시 구매'를 유도하며 실패해야 한다")
    void registerBuyBid_fail_induce_immediate_purchase() {
        // [Given] 1. 테스트를 위한 기초 데이터(유저, 상품) 수동 저장
        // 구매자 (나)
        Long buyerId = 10L;
        MarketUser buyer = MarketUser.builder()
                .id(buyerId) // 수동 ID 할당
                .nickname("buyer")
                .email("buyer@test.com")
                .role(Role.USER)
                .build();
        userRepository.save(buyer);

        // 판매자 (상대방)
        Long sellerId = 20L;
        MarketUser seller = MarketUser.builder()
                .id(sellerId) // 수동 ID 할당
                .nickname("seller")
                .email("seller@test.com")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        // 상품
        Long productId = 100L;
        MarketProduct product = MarketProduct.builder()
                .id(productId) // 수동 ID 할당
                .name("나이키 신발")
                .productNumber("NK-123")
                .productOption("270")
                .brandName("Nike")
                .categoryName("Shoes")
                .releasePrice(BigDecimal.valueOf(100000))
                .build();
        productRepository.save(product);


        // [Given] 2. 상황 조성: 누군가(Seller)가 30,000원에 팔겠다고(SELL) 올려둔 상태
        Bidding existingSellBid = Bidding.builder()
                .marketUser(seller)   // 위에서 저장한 seller 객체 사용
                .marketProduct(product) // 위에서 저장한 product 객체 사용
                .position(BiddingPosition.SELL)
                .status(BiddingStatus.PROCESS)
                .price(BigDecimal.valueOf(30000)) // 판매가 3만원
                .build();
        biddingRepository.save(existingSellBid); // Bidding은 GeneratedValue일 테니 ID 안 넣어도 됨


        // [When] 내가 30,000원에 사겠다고 입찰 시도 (이미 파는 사람이 있으니 에러 나야 함)
        BiddingRequestDto request = BiddingRequestDto.of(productId, BigDecimal.valueOf(30000), "270");

        // [Then]
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            usecase.registerBuyBid(buyerId, request);
        });

        assertThat(exception.getMessage()).isEqualTo(FailureCode.INVALID_BID_PRICE_BUY.getMessage());
    }

    @Test
    @DisplayName("잔액 부족 시 예외가 발생하고 DB에 저장되지 않아야 한다")
    void registerBuyBid_fail_rollback() {
        Long userId = 1L;
        BiddingRequestDto requestDto = BiddingRequestDto.of(1L, BigDecimal.valueOf(9999), "270");

        assertThrows(BadRequestException.class, () -> {
            usecase.registerBuyBid(userId, requestDto);
        });

        List<Bidding> all = biddingRepository.findAll();
        assertThat(all).isEmpty();
    }

}
