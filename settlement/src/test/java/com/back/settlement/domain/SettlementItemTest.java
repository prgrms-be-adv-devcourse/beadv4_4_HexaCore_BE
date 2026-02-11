package com.back.settlement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.back.settlement.domain.exception.InvalidSettlementItemStateException;
import com.back.settlement.fixture.SettlementItemFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SettlementItem 도메인 테스트")
class SettlementItemTest {

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTest {

        @Test
        @DisplayName("COLLECTED → INCLUDED 전이 성공")
        void included_fromCollected_success() {
            SettlementItem item = SettlementItemFixture.createCollectedItem(1L, 100L);

            item.included();

            assertThat(item.getStatus()).isEqualTo(SettlementItemStatus.INCLUDED);
        }

        @Test
        @DisplayName("INCLUDED 상태에서 included() 호출 시 예외 발생")
        void included_fromIncluded_throwsException() {
            SettlementItem item = SettlementItemFixture.createIncludedItem(1L, 100L);

            assertThatThrownBy(item::included)
                    .isInstanceOf(InvalidSettlementItemStateException.class);
        }
    }

    @Nested
    @DisplayName("SettlementItemStatus 테스트")
    class ItemStatusTest {

        @Test
        @DisplayName("COLLECTED에서 허용된 전이 검증")
        void collected_allowedTransitions() {
            assertThat(SettlementItemStatus.COLLECTED.canTransitionTo(SettlementItemStatus.INCLUDED)).isTrue();
            assertThat(SettlementItemStatus.COLLECTED.canTransitionTo(SettlementItemStatus.CANCELED)).isFalse();
            assertThat(SettlementItemStatus.COLLECTED.canTransitionTo(SettlementItemStatus.REFUNDED)).isFalse();
        }

        @Test
        @DisplayName("INCLUDED에서 허용된 전이 검증")
        void included_allowedTransitions() {
            assertThat(SettlementItemStatus.INCLUDED.canTransitionTo(SettlementItemStatus.CANCELED)).isTrue();
            assertThat(SettlementItemStatus.INCLUDED.canTransitionTo(SettlementItemStatus.REFUNDED)).isTrue();
            assertThat(SettlementItemStatus.INCLUDED.canTransitionTo(SettlementItemStatus.NEGATIVE)).isTrue();
            assertThat(SettlementItemStatus.INCLUDED.canTransitionTo(SettlementItemStatus.COLLECTED)).isFalse();
        }

        @Test
        @DisplayName("전이 불가")
        void terminalStates_cannotTransition() {
            assertThat(SettlementItemStatus.CANCELED.getAllowedTransitions()).isEmpty();
            assertThat(SettlementItemStatus.REFUNDED.getAllowedTransitions()).isEmpty();
            assertThat(SettlementItemStatus.NEGATIVE.getAllowedTransitions()).isEmpty();
        }
    }
}
