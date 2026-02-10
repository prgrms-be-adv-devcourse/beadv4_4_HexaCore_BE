package com.back.settlement.app.event.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.back.common.event.Envelope;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.event.payload.PayoutResultPayload;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import com.back.settlement.fixture.SettlementFixture;
import tools.jackson.databind.json.JsonMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("캐시 지급 요청 결과를 받는 단위 테스트")
class CashPayoutResultKafkaListenerTest {

    private static final JsonMapper jsonMapper = new JsonMapper();

    @Mock
    private SettlementRepository settlementRepository;

    private CashPayoutResultKafkaListener listener;

    @BeforeEach
    void setUp() {
        listener = new CashPayoutResultKafkaListener(settlementRepository, jsonMapper);
    }

    private Settlement createSettlement(Long id, SettlementStatus status) {
        return SettlementFixture.createSettlement(id, 1L, "판매자", status);
    }

    private String successMessage(Long settlementId) {
        return jsonMapper.writeValueAsString(
                Envelope.of("settlement.payout.result", new PayoutResultPayload(settlementId, true, null))
        );
    }

    private String failureMessage(Long settlementId, String reason) {
        return jsonMapper.writeValueAsString(
                Envelope.of("settlement.payout.result", new PayoutResultPayload(settlementId, false, reason))
        );
    }

    @Nested
    @DisplayName("캐시 지급 성공 시")
    class WhenPayoutSuccess {

        @Test
        @DisplayName("정산 상태를 COMPLETED로 변경하고 저장한다")
        void completesSettlementAndSaves() throws Exception {
            // given
            Long settlementId = 1L;
            Settlement settlement = createSettlement(settlementId, SettlementStatus.IN_PROGRESS);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(successMessage(settlementId));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
            then(settlementRepository).should().save(settlement);
        }
    }

    @Nested
    @DisplayName("캐시 지급 실패 시")
    class WhenPayoutFailure {

        @Test
        @DisplayName("정산 상태를 FAILED로 변경하고 저장한다")
        void failsSettlementWithReasonAndSaves() throws Exception {
            // given
            Long settlementId = 1L;
            String failReason = "잔액 부족";
            Settlement settlement = createSettlement(settlementId, SettlementStatus.IN_PROGRESS);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(failureMessage(settlementId, failReason));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);
            then(settlementRepository).should().save(settlement);
        }
    }

    @Nested
    @DisplayName("정산서를 찾을 수 없을 때")
    class WhenSettlementNotFound {

        @Test
        @DisplayName("저장하지 않고 조기 종료한다")
        void doesNothingWhenNotFound() throws Exception {
            // given
            Long settlementId = 999L;
            given(settlementRepository.findById(settlementId)).willReturn(Optional.empty());

            // when
            listener.listen(successMessage(settlementId));

            // then
            then(settlementRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("이미 정산 완료 상태일 때")
    class WhenSettlementAlreadyTerminal {

        @Test
        @DisplayName("COMPLETED 상태 → 중복 처리 방지, 아무 변경 없이 종료한다")
        void ignoresAlreadyCompletedSettlement() throws Exception {
            // given
            Long settlementId = 1L;
            Settlement settlement = createSettlement(settlementId, SettlementStatus.COMPLETED);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(successMessage(settlementId));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
            then(settlementRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("FAILED 상태 → 중복 처리 방지, 아무 변경 없이 종료한다")
        void ignoresAlreadyFailedSettlement() throws Exception {
            // given
            Long settlementId = 1L;
            Settlement settlement = createSettlement(settlementId, SettlementStatus.FAILED);
            given(settlementRepository.findById(settlementId)).willReturn(Optional.of(settlement));

            // when
            listener.listen(failureMessage(settlementId, "잔액 부족"));

            // then
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);
            then(settlementRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
        }
    }
}
