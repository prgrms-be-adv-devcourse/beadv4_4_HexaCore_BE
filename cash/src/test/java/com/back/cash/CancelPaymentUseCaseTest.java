package com.back.cash;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.app.usecase.CancelPaymentUseCase;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.domain.enums.RelType;
import com.back.cash.dto.request.PaymentCancelRequestDto;
import com.back.cash.dto.response.PaymentCancelResponseDto;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelPaymentUseCaseTest {

    @InjectMocks
    private CancelPaymentUseCase cancelPaymentUseCase;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletSupport walletSupport;

    @Mock
    private CashLogSupport cashLogSupport;

    private final Long userId = 1L;
    private final RelType relType = RelType.ORDER;
    private final Long relId = 100L;
    private final BigDecimal amount = new BigDecimal("10000.00");

    private PaymentCancelRequestDto req(BigDecimal requestAmount) {
        return new PaymentCancelRequestDto(userId, relType, relId, requestAmount);
    }

    private Payment createPayment(PaymentStatus status, boolean isReleased) {
        return Payment.builder()
                .id(1L)
                .userId(userId)
                .relType(relType)
                .relId(relId)
                .totalAmount(amount)
                .status(status)
                .releasedAt(isReleased ? LocalDateTime.now() : null)
                .build();
    }

    @Test
    @DisplayName("취소 성공(DONE): 시스템 지갑에서 인출하여 사용자 지갑으로 입금하고 상태를 변경한다")
    void cancel_success_done() {
        // given
        PaymentCancelRequestDto request = req(amount);
        Payment p = createPayment(PaymentStatus.DONE, false);

        Wallet buyerWallet = mock(Wallet.class);
        Wallet systemWallet = mock(Wallet.class);

        given(paymentRepository.findWithLockByRelTypeAndRelId(relType, relId))
                .willReturn(Optional.of(p));
        given(walletSupport.getUserWallet(userId)).willReturn(buyerWallet);
        given(walletSupport.getSystemWallet()).willReturn(systemWallet);

        // when
        PaymentCancelResponseDto response = cancelPaymentUseCase.execute(request);

        // then
        assertAll(
                () -> assertEquals(userId, response.userId()),
                () -> assertEquals(amount, response.refundedAmount()),
                () -> assertEquals(PaymentStatus.CANCELED, p.getStatus()),
                () -> assertTrue(p.isReleased()),
                () -> verify(systemWallet).withdraw(amount),
                () -> verify(buyerWallet).deposit(amount),
                () -> verify(cashLogSupport).recordCancelRefund(any(), any(), eq(amount), eq(relType), eq(relId))
        );
    }

    @Test
    @DisplayName("멱등 응답(CANCELED + releasedAt 존재): 추가 작업 없이 환급액을 반환한다")
    void cancel_idempotent_already_canceled_with_released() {
        // given
        PaymentCancelRequestDto request = req(amount);
        Payment p = createPayment(PaymentStatus.CANCELED, true); // 이미 취소 및 환불 완료

        given(paymentRepository.findWithLockByRelTypeAndRelId(relType, relId))
                .willReturn(Optional.of(p));

        // when
        PaymentCancelResponseDto response = cancelPaymentUseCase.execute(request);

        // then
        assertAll(
                () -> assertEquals(amount, response.refundedAmount()),
                () -> verifyNoInteractions(walletSupport, cashLogSupport) // 지갑 이동 및 로그 기록 없음 확인
        );
    }

    @Test
    @DisplayName("예외(CANCELED인데 releasedAt 없음): PAYMENT_STATE_INCONSISTENT")
    void cancel_canceled_without_released_inconsistent() {
        // given
        PaymentCancelRequestDto request = req(amount);
        Payment p = createPayment(PaymentStatus.CANCELED, false); // 상태는 취소인데 환불 기록 없음

        given(paymentRepository.findWithLockByRelTypeAndRelId(relType, relId))
                .willReturn(Optional.of(p));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> cancelPaymentUseCase.execute(request));
        assertEquals(FailureCode.PAYMENT_STATE_INCONSISTENT, ex.getFailureCode());
    }

    @Test
    @DisplayName("예외(DONE이 아닌 상태): INVALID_CANCEL")
    void cancel_not_done_invalid_cancel() {
        // given
        PaymentCancelRequestDto request = req(amount);
        Payment p = createPayment(PaymentStatus.READY, false); // DONE이 아닌 상태

        given(paymentRepository.findWithLockByRelTypeAndRelId(relType, relId))
                .willReturn(Optional.of(p));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> cancelPaymentUseCase.execute(request));
        assertEquals(FailureCode.INVALID_CANCEL, ex.getFailureCode());
    }

    @Test
    @DisplayName("예외(요청 userId 불일치): OWNER_MISMATCH")
    void cancel_owner_mismatch() {
        // given
        PaymentCancelRequestDto request = new PaymentCancelRequestDto(999L, relType, relId, amount);
        Payment p = createPayment(PaymentStatus.DONE, false);

        given(paymentRepository.findWithLockByRelTypeAndRelId(relType, relId))
                .willReturn(Optional.of(p));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> cancelPaymentUseCase.execute(request));
        assertEquals(FailureCode.OWNER_MISMATCH, ex.getFailureCode());
    }

    @Test
    @DisplayName("예외(요청 금액 불일치): AMOUNT_MISMATCH")
    void cancel_amount_mismatch() {
        // given
        PaymentCancelRequestDto request = req(new BigDecimal("5000.00")); // 잘못된 금액 요청
        Payment p = createPayment(PaymentStatus.DONE, false);

        given(paymentRepository.findWithLockByRelTypeAndRelId(relType, relId))
                .willReturn(Optional.of(p));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> cancelPaymentUseCase.execute(request));
        assertEquals(FailureCode.AMOUNT_MISMATCH, ex.getFailureCode());
    }

    @Test
    @DisplayName("예외(totalAmount 없음): PAYMENT_TOTAL_AMOUNT_MISSING")
    void cancel_total_amount_missing() {
        // given
        PaymentCancelRequestDto request = req(amount);
        Payment p = Payment.builder()
                .userId(userId)
                .relType(relType)
                .relId(relId)
                .totalAmount(null)
                .status(PaymentStatus.DONE)
                .build();

        given(paymentRepository.findWithLockByRelTypeAndRelId(relType, relId))
                .willReturn(Optional.of(p));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> cancelPaymentUseCase.execute(request));
        assertEquals(FailureCode.PAYMENT_TOTAL_AMOUNT_MISSING, ex.getFailureCode());
    }
}
