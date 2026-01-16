package com.back.settlement.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.back.common.exception.UnauthorizedException;
import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementItemStatus;
import com.back.settlement.domain.SettlementStatus;
import com.back.settlement.fixture.SettlementFixture;
import com.back.settlement.fixture.SettlementItemFixture;
import com.back.settlement.mapper.SettlementMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementUseCase 테스트")
class SettlementUseCaseTest {

    @InjectMocks
    private SettlementUseCase settlementUseCase;

    @Mock
    private SettlementSupport settlementSupport;

    @Mock
    private SettlementMapper settlementMapper;

    @Nested
    @DisplayName("getSettlements 메서드")
    class GetSettlementsTest {

        @Test
        @DisplayName("판매자 ID로 정산 목록을 조회한다")
        void getSettlements_Success() {
            // given
            Long sellerId = 1L;

            Settlement settlement =
                    SettlementFixture.createCompletedSettlement(1L, sellerId);

            SettlementResponse expectedResponse = SettlementResponse.builder()
                    .settlementId(1L)
                    .sellerId(sellerId)
                    .status(SettlementStatus.COMPLETED)
                    .build();

            given(settlementSupport.findBySellerId(sellerId))
                    .willReturn(List.of(settlement));
            given(settlementMapper.toSettlementResponse(settlement))
                    .willReturn(expectedResponse);

            // when
            List<SettlementResponse> result =
                    settlementUseCase.getSettlements(sellerId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).sellerId()).isEqualTo(sellerId);
        }

        @Test
        @DisplayName("정산 내역이 없으면 빈 리스트를 반환한다")
        void getSettlements_EmptyResult() {
            // given
            Long sellerId = 1L;

            given(settlementSupport.findBySellerId(sellerId))
                    .willReturn(List.of());

            // when
            List<SettlementResponse> result =
                    settlementUseCase.getSettlements(sellerId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSettlementItem 메서드")
    class GetSettlementItemTest {

        @Test
        @DisplayName("정산 항목을 정상 조회한다")
        void getSettlementItem_Success() {
            // given
            Long settlementItemId = 1L;
            Long sellerId = 100L;

            SettlementItem settlementItem =
                    SettlementItemFixture.createIncludedItem(settlementItemId, sellerId);

            SettlementItemResponse expectedResponse = SettlementItemResponse.builder()
                    .settlementItemId(settlementItemId)
                    .sellerId(sellerId)
                    .status(SettlementItemStatus.INCLUDED)
                    .build();

            given(settlementSupport.findSettlementItemById(settlementItemId))
                    .willReturn(settlementItem);
            given(settlementMapper.toSettlementItemResponse(settlementItem))
                    .willReturn(expectedResponse);

            // when
            SettlementItemResponse result =
                    settlementUseCase.getSettlementItem(settlementItemId, sellerId);

            // then
            assertThat(result.settlementItemId()).isEqualTo(settlementItemId);
            assertThat(result.sellerId()).isEqualTo(sellerId);
        }

        @Test
        @DisplayName("다른 판매자의 정산 항목 조회 시 예외가 발생한다")
        void getSettlementItem_UnauthorizedAccess() {
            // given
            Long settlementItemId = 1L;
            Long ownerSellerId = 100L;
            Long otherSellerId = 200L;

            SettlementItem settlementItem =
                    SettlementItemFixture.createIncludedItem(settlementItemId, ownerSellerId);

            given(settlementSupport.findSettlementItemById(settlementItemId))
                    .willReturn(settlementItem);

            // when & then
            assertThatThrownBy(() ->
                    settlementUseCase.getSettlementItem(settlementItemId, otherSellerId)
            ).isInstanceOf(UnauthorizedException.class);
        }
    }
}
