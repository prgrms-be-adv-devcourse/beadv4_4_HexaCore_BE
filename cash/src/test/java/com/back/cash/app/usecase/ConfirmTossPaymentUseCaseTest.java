package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.adapter.out.TossPaymentsClient;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.ConfirmPaymentSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.domain.enums.WalletType;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmTossPaymentUseCaseTest {

    private ConfirmTossPaymentUseCase confirmTossPaymentUseCase;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private WalletSupport walletSupport;
    @Mock
    private CashLogSupport cashLogSupport;
    @Mock
    private TossPaymentsClient tossPaymentsClient;

    @BeforeEach
    void setUp() {
        ConfirmPaymentSupport confirmPaymentSupport =
                new ConfirmPaymentSupport(paymentRepository, eventPublisher, walletSupport, cashLogSupport);
        confirmTossPaymentUseCase =
                new ConfirmTossPaymentUseCase(confirmPaymentSupport, tossPaymentsClient);
    }

    private static final Long USER_ID = 1L;
    private static final Long REL_ID = 100L;
    private static final RelType REL_TYPE = RelType.ORDER;
    private static final String ORDER_ID = "order_test_123";
    private static final String PAYMENT_KEY = "pk_test_abc";

    private Wallet buyerWallet;
    private Wallet systemWallet;

    // ========== validatePayment 검증 ==========

    @Test
    @DisplayName("[검증] orderId로 Payment 조회 실패 시 EntityNotFoundException")
    void execute_whenPaymentNotFound_thenThrowEntityNotFound() {
        // given
        TossConfirmRequest req = req(bd("18000"));
        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> confirmTossPaymentUseCase.execute(req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("[검증-멱등] 이미 DONE 상태면 토스 호출 없이 성공 응답 반환")
    void execute_whenAlreadyDone_thenReturnSuccessWithoutTossCall() {
        // given
        TossConfirmRequest req = req(bd("18000"));
        Payment payment = paymentBuilder(PaymentStatus.DONE).build();
        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.completedDto()).isNotNull();
        verifyNoInteractions(tossPaymentsClient);
        verifyNoInteractions(walletSupport);
    }

    @Test
    @DisplayName("[검증] FAIL 상태면 BadRequestException(INVALID_CONFIRM)")
    void execute_whenStatusFail_thenThrowInvalidConfirm() {
        // given
        TossConfirmRequest req = req(bd("18000"));
        Payment payment = paymentBuilder(PaymentStatus.FAIL).build();
        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> confirmTossPaymentUseCase.execute(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("[검증] CANCELED 상태면 BadRequestException(INVALID_CONFIRM)")
    void execute_whenStatusCanceled_thenThrowInvalidConfirm() {
        // given
        TossConfirmRequest req = req(bd("18000"));
        Payment payment = paymentBuilder(PaymentStatus.CANCELED).build();
        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> confirmTossPaymentUseCase.execute(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("[검증] pgAmount가 null이면 BadRequestException(INVALID_CONFIRM)")
    void execute_whenPgAmountNull_thenThrowInvalidConfirm() {
        // given
        TossConfirmRequest req = req(bd("18000"));
        Payment payment = Payment.builder()
                .userId(USER_ID).relType(REL_TYPE).relId(REL_ID)
                .tossOrderId(ORDER_ID)
                .totalAmount(bd("30000"))
                .walletUsedAmount(bd("12000"))
                .pgAmount(null)
                .status(PaymentStatus.READY)
                .build();
        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> confirmTossPaymentUseCase.execute(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("[검증] pgAmount가 0 이하면 BadRequestException(INVALID_CONFIRM)")
    void execute_whenPgAmountZeroOrNegative_thenThrowInvalidConfirm() {
        // given
        TossConfirmRequest req = req(bd("0"));
        Payment payment = Payment.builder()
                .userId(USER_ID).relType(REL_TYPE).relId(REL_ID)
                .tossOrderId(ORDER_ID)
                .totalAmount(bd("30000"))
                .walletUsedAmount(bd("30000"))
                .pgAmount(bd("0"))
                .status(PaymentStatus.READY)
                .build();
        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> confirmTossPaymentUseCase.execute(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("[검증] 요청 금액과 pgAmount 불일치 시 BadRequestException(AMOUNT_MISMATCH)")
    void execute_whenAmountMismatch_thenThrowAmountMismatch() {
        // given
        TossConfirmRequest req = req(bd("20000")); // pgAmount(18000)와 다른 금액
        Payment payment = paymentBuilder(PaymentStatus.READY).build();
        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> confirmTossPaymentUseCase.execute(req))
                .isInstanceOf(BadRequestException.class);
    }

    // ========== 토스 성공 → applySuccess ==========

    @Test
    @DisplayName("[성공] 토스 confirm 성공 → PG 충전 후 즉시 홀딩, DONE 처리, 성공 DTO 반환")
    void execute_whenTossSuccess_thenApplyPgTopUpAndHold_andMarkDone() {
        // given
        BigDecimal pgAmount = bd("18000");
        TossConfirmRequest req = req(pgAmount);
        Payment payment = paymentBuilder(PaymentStatus.READY).build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));
        givenWallets(bd("0")); // buyer balance 0 (예치금은 이미 PayAndHold에서 홀딩됨)

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then: 응답 검증
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.completedDto()).isNotNull();
        assertThat(result.completedDto().relType()).isEqualTo(REL_TYPE);
        assertThat(result.completedDto().relId()).isEqualTo(REL_ID);

        // then: Payment 상태
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getPaymentKey()).isEqualTo(PAYMENT_KEY);

        // then: PG 충전 → 즉시 홀딩 (buyer +18000 -18000 = 0, system +18000)
        assertThat(buyerWallet.getBalance()).isEqualByComparingTo("0");
        assertThat(systemWallet.getBalance()).isEqualByComparingTo("18000");

        // then: 로그 기록
        verify(cashLogSupport).recordUserPgTopUpLog(eq(buyerWallet), eq(pgAmount), eq(REL_TYPE), eq(REL_ID));
        verify(cashLogSupport).recordHoldingLog(eq(buyerWallet), eq(systemWallet), eq(pgAmount), eq(REL_TYPE), eq(REL_ID));

        // then: 토스 confirm 호출됨
        verify(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);

        // then: 이벤트 발행됨
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("[성공-멱등] applySuccess 시 이미 DONE이면 지갑 이동 없이 성공 응답 반환 (동시 요청 시나리오)")
    void execute_whenApplySuccessAlreadyDone_thenIdempotentResponse() {
        // given
        BigDecimal pgAmount = bd("18000");
        TossConfirmRequest req = req(pgAmount);

        // validatePayment 시점에는 READY → 통과
        Payment readyPayment = paymentBuilder(PaymentStatus.READY).build();
        // applySuccess 시점에는 이미 다른 요청이 먼저 DONE 처리 완료
        Payment donePayment = paymentBuilder(PaymentStatus.DONE).paymentKey(PAYMENT_KEY).build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID))
                .willReturn(Optional.of(readyPayment))
                .willReturn(Optional.of(donePayment));

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then
        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(walletSupport);
        verify(cashLogSupport, never()).recordUserPgTopUpLog(any(), any(), any(), any());
    }

    // ========== 토스 실패 → applyFailure ==========

    @Test
    @DisplayName("[실패] 토스 confirm 실패 + walletUsedAmount > 0 → 홀딩 해제 후 FAIL 처리")
    void execute_whenTossFail_withHeldAmount_thenReleaseAndMarkFail() {
        // given
        BigDecimal pgAmount = bd("18000");
        BigDecimal walletUsed = bd("12000");
        TossConfirmRequest req = req(pgAmount);
        Payment payment = Payment.builder()
                .userId(USER_ID).relType(REL_TYPE).relId(REL_ID)
                .tossOrderId(ORDER_ID)
                .totalAmount(bd("30000"))
                .walletUsedAmount(walletUsed)
                .pgAmount(pgAmount)
                .status(PaymentStatus.READY)
                .build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));
        doThrow(new RuntimeException("Toss API error"))
                .when(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);
        givenWallets(bd("0"), bd("12000")); // system에 12000 이미 홀딩됨

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then: 실패 응답
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.failedDto()).isNotNull();
        assertThat(result.failedDto().relType()).isEqualTo(REL_TYPE);
        assertThat(result.failedDto().relId()).isEqualTo(REL_ID);

        // then: Payment 상태
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
        assertThat(payment.isReleased()).isTrue();

        // then: 홀딩 해제 (system -12000, buyer +12000)
        assertThat(buyerWallet.getBalance()).isEqualByComparingTo("12000");
        assertThat(systemWallet.getBalance()).isEqualByComparingTo("0");

        // then: 해제 로그
        verify(cashLogSupport).recordReleaseOnPaymentFail(
                eq(buyerWallet), eq(systemWallet), eq(walletUsed), eq(REL_TYPE), eq(REL_ID));

        // then: 이벤트 발행됨
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("[실패] 토스 confirm 실패 + walletUsedAmount == 0 → 지갑 이동 없이 FAIL 처리")
    void execute_whenTossFail_withNoHeldAmount_thenMarkFailOnly() {
        // given
        BigDecimal pgAmount = bd("30000");
        TossConfirmRequest req = req(pgAmount);
        Payment payment = Payment.builder()
                .userId(USER_ID).relType(REL_TYPE).relId(REL_ID)
                .tossOrderId(ORDER_ID)
                .totalAmount(bd("30000"))
                .walletUsedAmount(bd("0"))
                .pgAmount(pgAmount)
                .status(PaymentStatus.READY)
                .build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));
        doThrow(new RuntimeException("Toss API error"))
                .when(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
        verifyNoInteractions(walletSupport);
        verify(cashLogSupport, never()).recordReleaseOnPaymentFail(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("[실패] 토스 confirm 실패 + 이미 released → 중복 해제 없이 FAIL 처리")
    void execute_whenTossFail_alreadyReleased_thenMarkFailOnly() {
        // given
        BigDecimal pgAmount = bd("18000");
        TossConfirmRequest req = req(pgAmount);
        Payment payment = Payment.builder()
                .userId(USER_ID).relType(REL_TYPE).relId(REL_ID)
                .tossOrderId(ORDER_ID)
                .totalAmount(bd("30000"))
                .walletUsedAmount(bd("12000"))
                .pgAmount(pgAmount)
                .status(PaymentStatus.READY)
                .releasedAt(LocalDateTime.now()) // 이미 해제됨
                .build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));
        doThrow(new RuntimeException("Toss API error"))
                .when(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
        verifyNoInteractions(walletSupport);
        verify(cashLogSupport, never()).recordReleaseOnPaymentFail(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("[실패-멱등] applyFailure 시 이미 DONE이면 성공 응답 반환 (동시 요청 시나리오)")
    void execute_whenApplyFailureButAlreadyDone_thenReturnSuccess() {
        // given
        BigDecimal pgAmount = bd("18000");
        TossConfirmRequest req = req(pgAmount);

        Payment readyPayment = paymentBuilder(PaymentStatus.READY).build();
        Payment donePayment = paymentBuilder(PaymentStatus.DONE).paymentKey(PAYMENT_KEY).build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID))
                .willReturn(Optional.of(readyPayment))
                .willReturn(Optional.of(donePayment));
        doThrow(new RuntimeException("Toss API error"))
                .when(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.completedDto()).isNotNull();
    }

    @Test
    @DisplayName("[실패-멱등] applyFailure 시 이미 FAIL이면 실패 응답 반환")
    void execute_whenApplyFailureButAlreadyFail_thenReturnFail() {
        // given
        BigDecimal pgAmount = bd("18000");
        TossConfirmRequest req = req(pgAmount);

        Payment readyPayment = paymentBuilder(PaymentStatus.READY).build();
        Payment failPayment = paymentBuilder(PaymentStatus.FAIL).build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID))
                .willReturn(Optional.of(readyPayment))
                .willReturn(Optional.of(failPayment));
        doThrow(new RuntimeException("Toss API error"))
                .when(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.failedDto()).isNotNull();
    }

    // ========== 타임아웃/네트워크 오류 → PENDING ==========

    @Test
    @DisplayName("[타임아웃] ResourceAccessException 발생 시 DB 변경 없이 PENDING 반환")
    void execute_whenTossTimeout_thenReturnPendingWithoutDbChange() {
        // given
        BigDecimal pgAmount = bd("18000");
        TossConfirmRequest req = req(pgAmount);
        Payment payment = paymentBuilder(PaymentStatus.READY).build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));
        doThrow(new ResourceAccessException("Read timed out"))
                .when(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then: PENDING 응답
        assertThat(result.isPending()).isTrue();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.completedDto()).isNull();
        assertThat(result.failedDto()).isNull();

        // then: Payment 상태 READY 그대로 유지
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);

        // then: 지갑/로그 변경 없음
        verifyNoInteractions(walletSupport);
        verify(cashLogSupport, never()).recordReleaseOnPaymentFail(any(), any(), any(), any(), any());

        // then: 이벤트 발행 안 됨
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("[타임아웃] 예치금 홀딩 상태에서 타임아웃 → 홀딩 해제하지 않고 READY 유지")
    void execute_whenTossTimeout_withHeldAmount_thenKeepHolding() {
        // given
        BigDecimal pgAmount = bd("18000");
        TossConfirmRequest req = req(pgAmount);
        Payment payment = Payment.builder()
                .userId(USER_ID).relType(REL_TYPE).relId(REL_ID)
                .tossOrderId(ORDER_ID)
                .totalAmount(bd("30000"))
                .walletUsedAmount(bd("12000"))
                .pgAmount(pgAmount)
                .status(PaymentStatus.READY)
                .build();

        given(paymentRepository.findWithLockByTossOrderId(ORDER_ID)).willReturn(Optional.of(payment));
        doThrow(new ResourceAccessException("Connection timed out"))
                .when(tossPaymentsClient).confirm(PAYMENT_KEY, ORDER_ID, pgAmount);

        // when
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        // then: READY 유지, 홀딩 해제 안 함
        assertThat(result.isPending()).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.isReleased()).isFalse();
        verifyNoInteractions(walletSupport);
    }

    // ========== 헬퍼 메서드 ==========

    private TossConfirmRequest req(BigDecimal amount) {
        return new TossConfirmRequest(PAYMENT_KEY, ORDER_ID, amount);
    }

    private Payment.PaymentBuilder paymentBuilder(PaymentStatus status) {
        return Payment.builder()
                .userId(USER_ID)
                .relType(REL_TYPE)
                .relId(REL_ID)
                .tossOrderId(ORDER_ID)
                .totalAmount(bd("30000"))
                .walletUsedAmount(bd("12000"))
                .pgAmount(bd("18000"))
                .status(status);
    }

    private void givenWallets(BigDecimal buyerBalance) {
        givenWallets(buyerBalance, BigDecimal.ZERO);
    }

    private void givenWallets(BigDecimal buyerBalance, BigDecimal systemBalance) {
        buyerWallet = Wallet.builder()
                .userId(USER_ID)
                .balance(buyerBalance)
                .walletType(WalletType.USER)
                .build();

        systemWallet = Wallet.builder()
                .balance(systemBalance)
                .walletType(WalletType.SYSTEM)
                .build();

        given(walletSupport.getUserWallet(USER_ID)).willReturn(buyerWallet);
        given(walletSupport.getSystemWallet()).willReturn(systemWallet);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
