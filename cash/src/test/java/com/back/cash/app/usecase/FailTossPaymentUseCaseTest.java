package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.domain.event.PaymentFailedEvent;
import com.back.cash.dto.request.TossFailRequestDto;
import com.back.common.code.FailureCode;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailTossPaymentUseCaseTest {

    @InjectMocks
    private FailTossPaymentUseCase failTossPaymentUseCase;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private WalletSupport walletSupport;

    @Mock
    private CashLogSupport cashLogSupport;

    private final String orderId = "toss-order-001";
    private final RelType relType = RelType.BIDDING;
    private final Long relId = 100L;
    private final Long userId = 1L;

    private TossFailRequestDto req() {
        return new TossFailRequestDto(orderId, "PAY_PROCESS_ABORTED", "사용자가 결제를 취소했습니다");
    }

    private Payment createPayment(PaymentStatus status, BigDecimal walletUsedAmount, boolean isReleased) {
        return Payment.builder()
                .id(1L)
                .userId(userId)
                .relType(relType)
                .relId(relId)
                .tossOrderId(orderId)
                .walletUsedAmount(walletUsedAmount)
                .status(status)
                .releasedAt(isReleased ? LocalDateTime.now() : null)
                .build();
    }

    @Test
    @DisplayName("실패 처리 성공(READY, 선홀딩 있음): FAIL 마킹 후 홀딩 해제 및 이벤트 발행")
    void fail_success_ready_with_held() {
        // given
        BigDecimal held = new BigDecimal("5000.00");
        Payment payment = createPayment(PaymentStatus.READY, held, false);
        Wallet buyerWallet = mock(Wallet.class);
        Wallet systemWallet = mock(Wallet.class);

        given(paymentRepository.findWithLockByTossOrderId(orderId)).willReturn(Optional.of(payment));
        given(walletSupport.getUserWallet(userId)).willReturn(buyerWallet);
        given(walletSupport.getSystemWallet()).willReturn(systemWallet);

        // when
        failTossPaymentUseCase.execute(req());

        // then
        assertAll(
                () -> assertEquals(PaymentStatus.FAIL, payment.getStatus()),
                () -> assertTrue(payment.isReleased()),
                () -> verify(systemWallet).withdraw(held),
                () -> verify(buyerWallet).deposit(held),
                () -> verify(cashLogSupport).recordReleaseOnPaymentFail(any(), any(), eq(held), eq(relType), eq(relId)),
                () -> verify(eventPublisher).publishEvent(any(PaymentFailedEvent.class))
        );
    }

    @Test
    @DisplayName("실패 처리 성공(READY, 선홀딩 없음): FAIL 마킹 후 지갑 이동 없이 이벤트 발행")
    void fail_success_ready_without_held() {
        // given
        Payment payment = createPayment(PaymentStatus.READY, null, false);

        given(paymentRepository.findWithLockByTossOrderId(orderId)).willReturn(Optional.of(payment));

        // when
        failTossPaymentUseCase.execute(req());

        // then
        assertAll(
                () -> assertEquals(PaymentStatus.FAIL, payment.getStatus()),
                () -> verifyNoInteractions(walletSupport, cashLogSupport),
                () -> verify(eventPublisher).publishEvent(any(PaymentFailedEvent.class))
        );
    }

    @Test
    @DisplayName("이벤트에 relType, relId, failReason이 올바르게 담긴다")
    void fail_event_payload_is_correct() {
        // given
        Payment payment = createPayment(PaymentStatus.READY, null, false);
        given(paymentRepository.findWithLockByTossOrderId(orderId)).willReturn(Optional.of(payment));
        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);

        // when
        failTossPaymentUseCase.execute(req());

        // then
        verify(eventPublisher).publishEvent(captor.capture());
        PaymentFailedEvent event = captor.getValue();
        assertAll(
                () -> assertEquals(relType, event.relType()),
                () -> assertEquals(relId, event.relId()),
                () -> assertEquals("사용자가 결제를 취소했습니다", event.failReason())
        );
    }

    @Test
    @DisplayName("멱등(DONE): 아무 처리 없이 종료, 이벤트 미발행")
    void fail_idempotent_done() {
        // given
        Payment payment = createPayment(PaymentStatus.DONE, null, false);
        given(paymentRepository.findWithLockByTossOrderId(orderId)).willReturn(Optional.of(payment));

        // when
        failTossPaymentUseCase.execute(req());

        // then
        assertAll(
                () -> assertEquals(PaymentStatus.DONE, payment.getStatus()),
                () -> verifyNoInteractions(walletSupport, cashLogSupport, eventPublisher)
        );
    }

    @Test
    @DisplayName("멱등(FAIL): 아무 처리 없이 종료, 이벤트 미발행")
    void fail_idempotent_already_failed() {
        // given
        Payment payment = createPayment(PaymentStatus.FAIL, null, false);
        given(paymentRepository.findWithLockByTossOrderId(orderId)).willReturn(Optional.of(payment));

        // when
        failTossPaymentUseCase.execute(req());

        // then
        assertAll(
                () -> assertEquals(PaymentStatus.FAIL, payment.getStatus()),
                () -> verifyNoInteractions(walletSupport, cashLogSupport, eventPublisher)
        );
    }

    @Test
    @DisplayName("멱등(CANCELED): 아무 처리 없이 종료, 이벤트 미발행")
    void fail_idempotent_canceled() {
        // given
        Payment payment = createPayment(PaymentStatus.CANCELED, null, true);
        given(paymentRepository.findWithLockByTossOrderId(orderId)).willReturn(Optional.of(payment));

        // when
        failTossPaymentUseCase.execute(req());

        // then
        assertAll(
                () -> assertEquals(PaymentStatus.CANCELED, payment.getStatus()),
                () -> verifyNoInteractions(walletSupport, cashLogSupport, eventPublisher)
        );
    }

    @Test
    @DisplayName("예외(req null): BAD_REQUEST")
    void fail_null_request() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> failTossPaymentUseCase.execute(null));
        assertEquals(FailureCode.BAD_REQUEST, ex.getFailureCode());
    }

    @Test
    @DisplayName("예외(orderId null): BAD_REQUEST")
    void fail_null_order_id() {
        TossFailRequestDto req = new TossFailRequestDto(null, "CODE", "msg");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> failTossPaymentUseCase.execute(req));
        assertEquals(FailureCode.BAD_REQUEST, ex.getFailureCode());
    }

    @Test
    @DisplayName("예외(orderId blank): BAD_REQUEST")
    void fail_blank_order_id() {
        TossFailRequestDto req = new TossFailRequestDto("  ", "CODE", "msg");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> failTossPaymentUseCase.execute(req));
        assertEquals(FailureCode.BAD_REQUEST, ex.getFailureCode());
    }

    @Test
    @DisplayName("예외(payment 없음): PAYMENT_NOT_FOUND")
    void fail_payment_not_found() {
        given(paymentRepository.findWithLockByTossOrderId(orderId)).willReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> failTossPaymentUseCase.execute(req()));
        assertEquals(FailureCode.PAYMENT_NOT_FOUND, ex.getFailureCode());
    }
}
