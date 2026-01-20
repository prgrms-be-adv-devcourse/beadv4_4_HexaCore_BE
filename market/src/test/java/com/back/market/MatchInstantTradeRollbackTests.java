package com.back.market;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.app.usecase.MatchInstantTradeUseCase;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.domain.enums.Role;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.mapper.BiddingMapper;
import com.back.market.mapper.MarketProductMapper;
import com.back.market.mapper.MarketUserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
// @Transactional을 제거하여 각 UseCase의 트랜잭션 결과가 DB에 즉시 반영되게 함
public class MatchInstantTradeRollbackTests {

    @Autowired
    private MatchInstantTradeUseCase matchInstantTradeUseCase;
    @Autowired private BiddingRepository biddingRepository;
    @Autowired private MarketUserRepository marketUserRepository;
    @Autowired private MarketProductRepository marketProductRepository;
    @Autowired private BiddingMapper biddingMapper;
    @Autowired private MarketUserMapper marketUserMapper;
    @Autowired private MarketProductMapper marketProductMapper;

    @Test
    @DisplayName("통합 검증: 결제 실패 시(9999원) 예외가 던져지고 입찰 상태는 다시 PROCESS로 롤백되어야 한다")
    void buyNow_integration_rollback_on_payment_fail() {
        // [Given] 9999원 세팅 (FakeCashClient 실패 조건)
        Long productId = 200L;
        Long sellerId = 3L;
        Long buyerId = 4L;
        setupBaseData(productId, sellerId, buyerId, "인천시");

        // 초기 상태 PROCESS인 입찰 생성
        Bidding sellBid = createBidding(productId, sellerId, 5000, BiddingPosition.SELL);

        BiddingRequestDto request = new BiddingRequestDto(productId, BigDecimal.valueOf(5000), "270");

        // [When] 결제 실패 상황 발생
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            matchInstantTradeUseCase.buyNow(buyerId, request); //
        });

        // [Then]
        // 1. 에러 코드 확인
        assertThat(exception.getMessage()).isEqualTo(FailureCode.WALLET_CHARGE_FAILED.getMessage());

        // 2. 롤백 검증: 입찰 상태 확인 ✅
        // 클래스에 @Transactional이 없으므로, 내부 트랜잭션의 롤백 결과가 즉시 DB에 반영됩니다.
        Bidding rolledBackBid = biddingRepository.findById(sellBid.getId()).orElseThrow();
        assertThat(rolledBackBid.getStatus()).isEqualTo(BiddingStatus.PROCESS); // ✅ 이제 드디어 PROCESS가 나옵니다!
    }

    // --- Helper Methods (데이터 정리를 위해 메서드 단위 Transactional 고려 가능) ---
    private void setupBaseData(Long productId, Long sellerId, Long buyerId, String buyerAddress) {
        MarketUser seller = marketUserMapper.toEntity(sellerId, Role.USER, "seller", "s@t.com", "판매자주소", "010-1234-5678", "image");
        marketUserRepository.save(seller);

        MarketUser buyer = marketUserMapper.toEntity(buyerId, Role.USER, "buyer", "b@t.com", buyerAddress, "010-1234-5678", "image");
        marketUserRepository.save(buyer);

        if (!marketProductRepository.existsById(productId)) {
            marketProductRepository.save(marketProductMapper.toEntity(productId, "N", "신발", "N1", "270", 100000L, "S", "image"));
        }
    }

    private Bidding createBidding(Long productId, Long userId, long price, BiddingPosition position) {
        MarketProduct product = marketProductRepository.findById(productId).orElseThrow();
        MarketUser user = marketUserRepository.findById(userId).orElseThrow();
        BiddingRequestDto requestDto = BiddingRequestDto.of(productId, BigDecimal.valueOf(price), "270");
        return biddingRepository.save(biddingMapper.toEntity(requestDto, user, product, position));
    }
}
