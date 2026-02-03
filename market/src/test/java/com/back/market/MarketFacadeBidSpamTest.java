package com.back.market;

import com.back.market.app.MarketFacade;
import com.back.market.app.usecase.RegisterBidUseCase;
import com.back.market.dto.request.BiddingRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;

/**
 * MarketFacade의 registerBuyBid / registerSellBid가
 * RegisterBidUseCase까지 정상 위임되는지 검증하는 단위 테스트
 *
 * @CheckBidSpam AOP 자체의 동작은 DetectorFacadeTest에서 검증 완료.
 */
@ExtendWith(MockitoExtension.class)
class MarketFacadeBidSpamTest {

    @InjectMocks
    private MarketFacade marketFacade;

    @Mock
    private RegisterBidUseCase registerBidUseCase;

    private static final Long USER_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final BiddingRequestDto BUY_REQUEST =
            BiddingRequestDto.of(PRODUCT_ID, BigDecimal.valueOf(50000), "270");
    private static final BiddingRequestDto SELL_REQUEST =
            BiddingRequestDto.of(PRODUCT_ID, BigDecimal.valueOf(150000), "270");

    @Nested
    @DisplayName("정상 입찰 등록")
    class WhenNotSpam {

        @Test
        @DisplayName("구매 입찰: UseCase까지 정상 호출되어야 한다")
        void should_call_register_buy_bid() {
            assertThatCode(() -> marketFacade.registerBuyBid(USER_ID, BUY_REQUEST))
                    .doesNotThrowAnyException();

            verify(registerBidUseCase).registerBuyBid(USER_ID, BUY_REQUEST);
        }

        @Test
        @DisplayName("판매 입찰: UseCase까지 정상 호출되어야 한다")
        void should_call_register_sell_bid() {
            assertThatCode(() -> marketFacade.registerSellBid(USER_ID, SELL_REQUEST))
                    .doesNotThrowAnyException();

            verify(registerBidUseCase).registerSellBid(USER_ID, SELL_REQUEST);
        }
    }
}
