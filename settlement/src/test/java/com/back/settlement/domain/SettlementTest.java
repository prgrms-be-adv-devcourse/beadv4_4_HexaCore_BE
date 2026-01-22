package com.back.settlement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.back.settlement.domain.event.SettlementFailedEvent;
import com.back.settlement.domain.event.SettlementHoldEvent;
import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import com.back.settlement.domain.event.SettlementStartedEvent;
import com.back.settlement.domain.exception.InvalidSettlementStateException;
import com.back.settlement.fixture.SettlementFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Settlement 도메인 테스트")
class SettlementTest {

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTest {

        @Test
        @DisplayName("PENDING → IN_PROGRESS 전이 성공")
        void start_fromPending_success() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);

            // when
            settlement.start();

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("IN_PROGRESS → COMPLETED 전이 성공")
        void complete_fromInProgress_success() {
            // given
            Settlement settlement = SettlementFixture.createSettlement(
                    1L, 100L, "TestSeller", SettlementStatus.IN_PROGRESS);

            // when
            settlement.complete();

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
            assertThat(settlement.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING -> COMPLETED 예외 발생")
        void complete_fromPending_throwsException() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);

            // when & then
            assertThatThrownBy(settlement::complete).isInstanceOf(InvalidSettlementStateException.class);
        }

        @Test
        @DisplayName("보류 처리 성공")
        void hold_success() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);

            // when
            settlement.hold("판매자 계정 검토 필요");

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.HOLD);
        }

        @Test
        @DisplayName("보류 사유 없이 호출 시 기본 사유 적용")
        void hold_withoutReason_usesDefaultReason() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);
            settlement.clearDomainEvents();

            // when
            settlement.hold(null);

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.HOLD);
            SettlementHoldEvent event = (SettlementHoldEvent) settlement.getDomainEvents().get(0);
            assertThat(event.reason()).isEqualTo("판매자 계좌 정보 없음");
        }

        @Test
        @DisplayName("실패 처리 성공")
        void fail_success() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);

            // when
            settlement.fail("Cash 지급 실패");

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("이벤트 발행 테스트")
    class EventPublishTest {

        @Test
        @DisplayName("start() 호출 시")
        void start_registersEvent() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);
            settlement.clearDomainEvents();

            // when
            settlement.start();

            // then
            assertThat(settlement.getDomainEvents()).hasSize(1);
            assertThat(settlement.getDomainEvents().get(0)).isInstanceOf(SettlementStartedEvent.class);
        }

        @Test
        @DisplayName("complete() 호출 시")
        void complete_registersEvent() {
            // given
            Settlement settlement = SettlementFixture.createSettlement(
                    1L, 100L, "TestSeller", SettlementStatus.IN_PROGRESS);
            settlement.clearDomainEvents();

            // when
            settlement.complete();

            // then
            assertThat(settlement.getDomainEvents()).hasSize(1);
            assertThat(settlement.getDomainEvents().get(0))
                    .isInstanceOf(SettlementInternalCompletedEvent.class);
        }

        @Test
        @DisplayName("hold() 호출 시")
        void hold_registersEvent() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);
            settlement.clearDomainEvents();

            // when
            settlement.hold("테스트 사유");

            // then
            assertThat(settlement.getDomainEvents()).hasSize(1);
            SettlementHoldEvent event = (SettlementHoldEvent) settlement.getDomainEvents().get(0);
            assertThat(event.reason()).isEqualTo("테스트 사유");
        }

        @Test
        @DisplayName("fail() 호출 시 ")
        void fail_registersEvent() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);
            settlement.clearDomainEvents();

            // when
            settlement.fail("실패 사유");

            // then
            assertThat(settlement.getDomainEvents()).hasSize(1);
            SettlementFailedEvent event = (SettlementFailedEvent) settlement.getDomainEvents().get(0);
            assertThat(event.reason()).isEqualTo("실패 사유");
        }

        @Test
        @DisplayName("clearDomainEvents() 검증")
        void clearDomainEvents_clearsAllEvents() {
            // given
            Settlement settlement = SettlementFixture.createPendingSettlement(1L, 100L);
            settlement.start();
            assertThat(settlement.getDomainEvents()).isNotEmpty();

            // when
            settlement.clearDomainEvents();

            // then
            assertThat(settlement.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("SettlementStatus 테스트")
    class SettlementStatusTest {

        @Test
        @DisplayName("PENDING에서 허용된 전이 상태 검증")
        void pending_allowedTransitions() {
            assertThat(SettlementStatus.PENDING.canTransitionTo(SettlementStatus.IN_PROGRESS)).isTrue();
            assertThat(SettlementStatus.PENDING.canTransitionTo(SettlementStatus.HOLD)).isTrue();
            assertThat(SettlementStatus.PENDING.canTransitionTo(SettlementStatus.FAILED)).isTrue();
            assertThat(SettlementStatus.PENDING.canTransitionTo(SettlementStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("COMPLETED는 최종 상태")
        void completed_isTerminal() {
            assertThat(SettlementStatus.COMPLETED.isTerminal()).isTrue();
            assertThat(SettlementStatus.COMPLETED.getAllowedTransitions()).isEmpty();
        }

        @Test
        @DisplayName("FAILED는 최종 상태")
        void failed_isTerminal() {
            assertThat(SettlementStatus.FAILED.isTerminal()).isTrue();
            assertThat(SettlementStatus.FAILED.getAllowedTransitions()).isEmpty();
        }
    }
}
