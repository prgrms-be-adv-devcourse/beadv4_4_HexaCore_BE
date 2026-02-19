package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PayoutRepository;
import com.back.cash.domain.Payout;
import com.back.cash.domain.enums.PayoutStatus;
import com.back.cash.domain.event.CashPayoutRequestedCommand;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.back.common.exception.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;


@SpringBootTest
@Slf4j
class CashPayoutUseCaseTest {

    @Autowired
    CashPayoutUseCase cashPayoutUseCase;

    @MockitoSpyBean
    PayoutRepository payoutRepository;

    private static final Long TEST_SETTLEMENT_ID = 99999L;
    private static final Long DONE_SETTLEMENT_ID = 99991L;
    private static final Long PROCESSING_SETTLEMENT_ID = 99992L;
    private static final Long NEW_SETTLEMENT_ID = 99993L;
    private static final Long DUPLICATE_CHECK_ID = 99994L;

    @BeforeEach
    void setUp() {
        deleteBySettlementId(TEST_SETTLEMENT_ID);
        deleteBySettlementId(DONE_SETTLEMENT_ID);
        deleteBySettlementId(PROCESSING_SETTLEMENT_ID);
        deleteBySettlementId(NEW_SETTLEMENT_ID);
        deleteBySettlementId(DUPLICATE_CHECK_ID);
    }

    @Test
    @DisplayName("중복 settlementId로 저장 시 DataIntegrityViolationException이 발생한다")
    void duplicateSettlementId_throwsDataIntegrityViolationException() {

        // given
        // 동일한 settlementId로 Payout을 미리 저장 → unique constraint 위반 유도
        Payout existing = Payout.builder()
                .settlementId(TEST_SETTLEMENT_ID)
                .payeeId(1L)
                .totalGrossAmount(new BigDecimal("10000"))
                .totalNetAmount(new BigDecimal("9000"))
                .totalFeeAmount(new BigDecimal("1000"))
                .status(PayoutStatus.PROCESSING)
                .build();
        payoutRepository.save(existing);

        // existsBySettlementId를 false로 강제 → race condition 시뮬레이션
        doReturn(false).when(payoutRepository).existsBySettlementId(TEST_SETTLEMENT_ID);

        CashPayoutRequestedCommand event = new CashPayoutRequestedCommand(
                TEST_SETTLEMENT_ID, 1L,
                new BigDecimal("10000"),
                new BigDecimal("9000"),
                new BigDecimal("1000")
        );

        // when & then
        // try-catch 제거 후 DataIntegrityViolationException이 그대로 전파됨
        assertThatThrownBy(() -> cashPayoutUseCase.execute(event))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("saveFailedPayout: 기존 상태가 DONE이면 실패로 덮어쓰지 않는다")
    void saveFailedPayout_done_doesNotOverride() {
        Payout done = Payout.builder()
                .settlementId(DONE_SETTLEMENT_ID)
                .payeeId(1L)
                .totalGrossAmount(new BigDecimal("10000"))
                .totalNetAmount(new BigDecimal("9000"))
                .totalFeeAmount(new BigDecimal("1000"))
                .status(PayoutStatus.DONE)
                .failReason("old-reason")
                .build();
        payoutRepository.save(done);

        cashPayoutUseCase.saveFailedPayout(event(DONE_SETTLEMENT_ID), "new-reason");

        Payout found = payoutRepository.findBySettlementId(DONE_SETTLEMENT_ID).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(PayoutStatus.DONE);
        assertThat(found.getFailReason()).isEqualTo("old-reason");
    }

    @Test
    @DisplayName("saveFailedPayout: 기존 상태가 PROCESSING이면 FAILED로 변경하고 사유를 기록한다")
    void saveFailedPayout_processing_marksFailed() {
        Payout processing = Payout.builder()
                .settlementId(PROCESSING_SETTLEMENT_ID)
                .payeeId(1L)
                .totalGrossAmount(new BigDecimal("10000"))
                .totalNetAmount(new BigDecimal("9000"))
                .totalFeeAmount(new BigDecimal("1000"))
                .status(PayoutStatus.PROCESSING)
                .build();
        payoutRepository.save(processing);

        cashPayoutUseCase.saveFailedPayout(event(PROCESSING_SETTLEMENT_ID), "validation-failed");

        Payout found = payoutRepository.findBySettlementId(PROCESSING_SETTLEMENT_ID).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(PayoutStatus.FAILED);
        assertThat(found.getFailReason()).isEqualTo("validation-failed");
    }

    @Test
    @DisplayName("saveFailedPayout: 기존 레코드가 없으면 FAILED 상태로 신규 생성한다")
    void saveFailedPayout_noRow_createsFailed() {
        cashPayoutUseCase.saveFailedPayout(event(NEW_SETTLEMENT_ID), "first-fail");

        Payout found = payoutRepository.findBySettlementId(NEW_SETTLEMENT_ID).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(PayoutStatus.FAILED);
        assertThat(found.getFailReason()).isEqualTo("first-fail");
    }

    @Test
    @DisplayName("금액이 null이면 BadRequestException이 발생한다")
    void nullAmount_throwsBadRequestException() {
        CashPayoutRequestedCommand event = new CashPayoutRequestedCommand(
                88888L, 1L,
                null,
                new BigDecimal("9000"),
                new BigDecimal("1000")
        );

        assertThatThrownBy(() -> cashPayoutUseCase.execute(event))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("금액이 음수이면 BadRequestException이 발생한다")
    void negativeAmount_throwsBadRequestException() {
        CashPayoutRequestedCommand event = new CashPayoutRequestedCommand(
                88887L, 1L,
                new BigDecimal("10000"),
                new BigDecimal("-1"),
                new BigDecimal("1000")
        );

        assertThatThrownBy(() -> cashPayoutUseCase.execute(event))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("이미 처리된 settlementId면 아무 일도 하지 않고 정상 종료한다")
    void duplicateSettlementId_returnsWithoutAction() {
        Payout existing = Payout.builder()
                .settlementId(DUPLICATE_CHECK_ID)
                .payeeId(1L)
                .totalGrossAmount(new BigDecimal("10000"))
                .totalNetAmount(new BigDecimal("9000"))
                .totalFeeAmount(new BigDecimal("1000"))
                .status(PayoutStatus.DONE)
                .build();
        payoutRepository.save(existing);

        assertThatCode(() -> cashPayoutUseCase.execute(event(DUPLICATE_CHECK_ID)))
                .doesNotThrowAnyException();

        Payout found = payoutRepository.findBySettlementId(DUPLICATE_CHECK_ID).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(PayoutStatus.DONE);
    }

    @Test
    @DisplayName("검증 금액 합계가 맞지 않으면 BadRequestException이 발생한다.")
    void validateAmounts_InvalidGrossSum_Fail() {
        CashPayoutRequestedCommand event = new CashPayoutRequestedCommand(
                88887L, 1L,
                new BigDecimal("10000"),
                new BigDecimal("2000"),
                new BigDecimal("1000")
        );

        assertThatThrownBy(() -> cashPayoutUseCase.execute(event))
                .isInstanceOf(BadRequestException.class);
    }

    private CashPayoutRequestedCommand event(Long settlementId) {
        return new CashPayoutRequestedCommand(
                settlementId, 1L,
                new BigDecimal("10000"),
                new BigDecimal("9000"),
                new BigDecimal("1000")
        );
    }

    private void deleteBySettlementId(Long settlementId) {
        payoutRepository.findBySettlementId(settlementId)
                .ifPresent(existing -> payoutRepository.deleteById(existing.getId()));
    }
}
