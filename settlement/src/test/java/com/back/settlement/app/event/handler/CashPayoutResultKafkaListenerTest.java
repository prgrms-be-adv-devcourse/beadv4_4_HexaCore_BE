package com.back.settlement.app.event.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.back.common.event.Envelope;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.event.payload.PayoutResultPayload;
import com.back.settlement.app.support.DomainEventPublisher;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("캐시 지급 요청 결과를 받는 단위 테스트")
class CashPayoutResultKafkaListenerTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private CashPayoutResultKafkaListener listener;

    private Settlement createSettlement(Long id, SettlementStatus status) {
        Settlement settlement = Settlement.create(
                new com.back.settlement.app.dto.request.SettlementRequest(
                        1L, "판매자", LocalDateTime.now().minusDays(7), LocalDateTime.now(),
                        BigDecimal.valueOf(100000), BigDecimal.valueOf(10000), BigDecimal.valueOf(90000)
                )
        );
        ReflectionTestUtils.setField(settlement, "id", id);
        ReflectionTestUtils.setField(settlement, "status", status);
        return settlement;
    }

    private Envelope<PayoutResultPayload> successEvent(Long settlementId) {
        return Envelope.of(
                "settlement.payout.result",
                new PayoutResultPayload(settlementId, true, null)
        );
    }

    private Envelope<PayoutResultPayload> failureEvent(Long settlementId, String reason) {
        return Envelope.of(
                "settlement.payout.result",
                new PayoutResultPayload(settlementId, false, reason)
        );
    }

    @Nested
    @DisplayName("캐시 지급 성공 시")
    class WhenPayoutSuccess {

        @Test
        @DisplayName("정산 상태를 COMPLETED로 변경하고 도메인 이벤트를 발행한다")
        void completesSettlementAndPublishesEvents() {
            // given
            Long settlementId = 1L;
            Settlement settlement = createSettlement(settlementId, SettlementStatus.IN_PROGRESS);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(successEvent(settlementId));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
            then(domainEventPublisher).should().publishEvents(settlement);
        }
    }

    @Nested
    @DisplayName("캐시 지급 실패 시")
    class WhenPayoutFailure {

        @Test
        @DisplayName("정산 상태를 FAILED로 변경하고 실패 사유와 함께 도메인 이벤트를 발행한다")
        void failsSettlementWithReasonAndPublishesEvents() {
            // given
            Long settlementId = 1L;
            String failReason = "잔액 부족";
            Settlement settlement = createSettlement(settlementId, SettlementStatus.IN_PROGRESS);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(failureEvent(settlementId, failReason));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);
            then(domainEventPublisher).should().publishEvents(settlement);
        }
    }

    @Nested
    @DisplayName("정산서를 찾을 수 없을 때")
    class WhenSettlementNotFound {

        @Test
        @DisplayName("도메인 이벤트를 발행하지 않고 조기 종료한다")
        void doesNothingWhenNotFound() {
            // given
            Long settlementId = 999L;
            given(settlementRepository.findById(settlementId)).willReturn(Optional.empty());

            // when
            listener.listen(successEvent(settlementId));

            // then
            then(domainEventPublisher).should(never()).publishEvents(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("이미 정산 완료 상태일 때")
    class WhenSettlementAlreadyTerminal {

        @Test
        @DisplayName("COMPLETED 상태 → 중복 처리 방지, 아무 변경 없이 종료한다")
        void ignoresAlreadyCompletedSettlement() {
            // given
            Long settlementId = 1L;
            Settlement settlement = createSettlement(settlementId, SettlementStatus.COMPLETED);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(successEvent(settlementId));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
            then(domainEventPublisher).should(never()).publishEvents(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("FAILED 상태 → 중복 처리 방지, 아무 변경 없이 종료한다")
        void ignoresAlreadyFailedSettlement() {
            // given
            Long settlementId = 1L;
            Settlement settlement = createSettlement(settlementId, SettlementStatus.FAILED);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(failureEvent(settlementId, "잔액 부족"));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);
            then(domainEventPublisher).should(never()).publishEvents(org.mockito.ArgumentMatchers.any());
        }
    }
}
