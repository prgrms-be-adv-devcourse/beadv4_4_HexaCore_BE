package com.back.settlement.app.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.back.common.exception.EntityNotFoundException;
import com.back.settlement.adapter.out.SettlementItemRepository;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.fixture.SettlementFixture;
import com.back.settlement.fixture.SettlementItemFixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementSupport 테스트")
class SettlementSupportTest {

    @InjectMocks
    private SettlementSupport settlementSupport;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementItemRepository settlementItemRepository;

    @Nested
    @DisplayName("findBySellerId 메서드")
    class FindBySellerIdTest {

        @Test
        @DisplayName("판매자 ID로 정산 목록을 조회한다")
        void findBySellerId_Success() {
            // given
            Long sellerId = 100L;

            Settlement settlement1 = SettlementFixture.createCompletedSettlement(1L, sellerId);
            Settlement settlement2 = SettlementFixture.createPendingSettlement(2L, sellerId);

            given(settlementRepository.findBySellerId(eq(sellerId)))
                    .willReturn(List.of(settlement1, settlement2));

            // when
            List<Settlement> result = settlementSupport.findBySellerId(sellerId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .allSatisfy(settlement ->
                            assertThat(settlement.getSellerId()).isEqualTo(sellerId)
                    );
        }

        @Test
        @DisplayName("정산 내역이 없는 판매자는 빈 리스트를 반환한다")
        void findBySellerId_EmptyResult() {
            // given
            Long sellerId = 999L;

            given(settlementRepository.findBySellerId(eq(sellerId)))
                    .willReturn(List.of());

            // when
            List<Settlement> result = settlementSupport.findBySellerId(sellerId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findSettlementItemById 메서드")
    class FindSettlementItemByIdTest {

        @Test
        @DisplayName("정산 항목 ID로 정산 항목을 조회한다")
        void findSettlementItemById_Success() {
            // given
            Long settlementItemId = 1L;
            SettlementItem expectedItem =
                    SettlementItemFixture.createIncludedItem(settlementItemId, 100L);

            given(settlementItemRepository.findById(settlementItemId))
                    .willReturn(Optional.of(expectedItem));

            // when
            SettlementItem result =
                    settlementSupport.findSettlementItemById(settlementItemId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(settlementItemId);
        }

        @Test
        @DisplayName("존재하지 않는 정산 항목 ID로 조회하면 예외가 발생한다")
        void findSettlementItemById_NotFound() {
            // given
            Long nonExistentId = 999L;

            given(settlementItemRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    settlementSupport.findSettlementItemById(nonExistentId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}
