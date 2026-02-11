package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.domain.enums.WalletType;
import com.back.cash.mapper.PaymentMapper;
import com.back.common.dto.cash.enums.PayAndHoldStatus;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayAndHoldUseCaseTest {

    @InjectMocks
    private PayAndHoldUseCase payAndHoldUseCase;

    @Mock
    private WalletSupport walletSupport;
    @Mock
    private CashLogSupport cashLogSupport;
    @Mock
    private PaymentRepository paymentRepository;

    private Long buyerId;
    private Long relId;
    private RelType relType;
    private String orderName;

    private Wallet buyerWallet;
    private Wallet systemWallet;

    @BeforeEach
    void setUp() {
        buyerId = 1L;
        relId = 100L;
        relType = RelType.ORDER;
        orderName = "상품";
    }

    @Test
    @DisplayName("[예치금 충분] balance >= total이면 예치금만으로 결제(PAID) + DONE 처리되고 홀딩 로그가 기록된다")
    void execute_whenBalanceGreaterOrEqualTotal_thenPaidDone_andHoldLogged() {
        // given
        BigDecimal balance = bd("50000");
        BigDecimal total = bd("30000");

        PayAndHoldRequestDto dto = dto(total);
        Payment payment = givenNewPayment(dto);
        givenWallets(balance);

        // when
        PayAndHoldResponseDto result = payAndHoldUseCase.execute(dto);

        // then: 지갑 이동(총액만큼 홀딩)
        assertThat(buyerWallet.getBalance()).isEqualByComparingTo("20000");
        assertThat(systemWallet.getBalance()).isEqualByComparingTo("30000");

        // then: 응답
        assertThat(result.status()).isEqualTo(PayAndHoldStatus.PAID);
        assertThat(result.walletUsedAmount()).isEqualByComparingTo("30000");
        assertThat(result.pgRequiredAmount()).isEqualByComparingTo("0");
        assertThat(result.tossOrderId()).isNull();

        // then: payment 상태
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getWalletUsedAmount()).isEqualByComparingTo("30000");
        assertThat(payment.getPgAmount()).isEqualByComparingTo("0");

        // then: 홀딩 로그 기록(총액만큼)
        verifyHoldingLogged("30000");
    }

    @Test
    @DisplayName("[예치금 부분 부족] 0 < balance < total이면 부분 홀딩 후 REQUIRES_PG 응답을 반환하고 tossOrderId가 발급된다")
    void execute_whenBalancePositiveButLessThanTotal_thenRequiresPg_andPartialHoldLogged() {
        // given
        BigDecimal balance = bd("12000");
        BigDecimal total = bd("30000");

        PayAndHoldRequestDto dto = dto(total);
        Payment payment = givenNewPayment(dto);
        givenWallets(balance);

        // when
        PayAndHoldResponseDto result = payAndHoldUseCase.execute(dto);

        // then: 부분 홀딩(balance만큼)
        assertThat(buyerWallet.getBalance()).isEqualByComparingTo("0");
        assertThat(systemWallet.getBalance()).isEqualByComparingTo("12000");

        // then: 응답(REQUIRES_PG)
        assertThat(result.status()).isEqualTo(PayAndHoldStatus.REQUIRES_PG);
        assertThat(result.walletUsedAmount()).isEqualByComparingTo("12000");
        assertThat(result.pgRequiredAmount()).isEqualByComparingTo("18000");
        assertThat(result.tossOrderId()).isNotNull();

        // then: payment 상태(READY 유지)
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getWalletUsedAmount()).isEqualByComparingTo("12000");
        assertThat(payment.getPgAmount()).isEqualByComparingTo("18000");
        assertThat(payment.getTossOrderId()).isNotNull();

        // then: 홀딩 로그는 balance만큼
        verifyHoldingLogged("12000");
    }

    @Test
    @DisplayName("[예치금 부족-전액 PG] balance == 0이면 홀딩 없이 REQUIRES_PG 응답을 반환하고 tossOrderId가 발급된다")
    void execute_whenBalanceZero_thenRequiresPg_andNoHoldingLog() {
        // given
        BigDecimal balance = bd("0");
        BigDecimal total = bd("30000");

        PayAndHoldRequestDto dto = dto(total);
        Payment payment = givenNewPayment(dto);
        givenWallets(balance);

        // when
        PayAndHoldResponseDto result = payAndHoldUseCase.execute(dto);

        // then: 홀딩 없음
        assertThat(buyerWallet.getBalance()).isEqualByComparingTo("0");
        assertThat(systemWallet.getBalance()).isEqualByComparingTo("0");

        // then: 응답(REQUIRES_PG)
        assertThat(result.status()).isEqualTo(PayAndHoldStatus.REQUIRES_PG);
        assertThat(result.walletUsedAmount()).isEqualByComparingTo("0");
        assertThat(result.pgRequiredAmount()).isEqualByComparingTo("30000");
        assertThat(result.tossOrderId()).isNotNull();

        // then: payment 상태(READY 유지)
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getWalletUsedAmount()).isEqualByComparingTo("0");
        assertThat(payment.getPgAmount()).isEqualByComparingTo("30000");
        assertThat(payment.getTossOrderId()).isNotNull();

        // then: 홀딩 로그 없음
        verify(cashLogSupport, never()).recordHoldingLog(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("[멱등 재호출] 이미 PG 필요 상태면 재호출 시 홀딩/로그 없이 동일 REQUIRES_PG 응답을 반환한다")
    void execute_whenAlreadyRequiresPgState_thenIdempotentResponse_andNoMoreHolding() {
        // given
        BigDecimal total = bd("30000");
        PayAndHoldRequestDto dto = dto(total);

        Payment existing = Payment.builder()
                .id(10L)
                .userId(buyerId)
                .relType(relType)
                .relId(relId)
                .totalAmount(total)
                .walletUsedAmount(bd("12000"))
                .pgAmount(bd("18000"))
                .tossOrderId("existing-order-id")
                .status(PaymentStatus.READY)
                .build();

        given(paymentRepository.findByRelTypeAndRelId(relType, relId)).willReturn(Optional.of(existing));

        // when
        PayAndHoldResponseDto result = payAndHoldUseCase.execute(dto);

        // then: wallet/로그 로직 미진입
        verify(walletSupport, never()).getUserWallet(anyLong());
        verify(walletSupport, never()).getSystemWallet();
        verify(cashLogSupport, never()).recordHoldingLog(any(), any(), any(), any(), any());

        // then: 응답 동일
        assertThat(result.status()).isEqualTo(PayAndHoldStatus.REQUIRES_PG);
        assertThat(result.walletUsedAmount()).isEqualByComparingTo("12000");
        assertThat(result.pgRequiredAmount()).isEqualByComparingTo("18000");
        assertThat(result.tossOrderId()).isEqualTo("existing-order-id");
    }

    @Test
    @DisplayName("[금액 불일치] 동일 relType/relId 재호출인데 totalAmount가 다르면 AMOUNT_MISMATCH 예외가 발생한다")
    void execute_whenTotalAmountMismatch_thenThrowAmountMismatch() {
        // given
        PayAndHoldRequestDto first = dto(bd("30000"));
        PayAndHoldRequestDto second = dto(bd("31000"));

        Payment existing = PaymentMapper.toPayment(first);
        given(paymentRepository.findByRelTypeAndRelId(relType, relId)).willReturn(Optional.of(existing));

        // when / then
        assertThatThrownBy(() -> payAndHoldUseCase.execute(second))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("결제 금액이 일치하지 않습니다.");
    }

    private PayAndHoldRequestDto dto(BigDecimal total) {
        return new PayAndHoldRequestDto(buyerId, total, orderName, relType, relId);
    }

    private Payment givenNewPayment(PayAndHoldRequestDto dto) {
        Payment payment = PaymentMapper.toPayment(dto);

        given(paymentRepository.findByRelTypeAndRelId(relType, relId)).willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);

        return payment;
    }

    private void givenWallets(BigDecimal buyerBalance) {
        buyerWallet = Wallet.builder()
                .userId(buyerId)
                .balance(buyerBalance)
                .walletType(WalletType.USER)
                .build();

        systemWallet = Wallet.builder()
                .balance(BigDecimal.ZERO)
                .walletType(WalletType.SYSTEM)
                .build();

        given(walletSupport.getUserWallet(buyerId)).willReturn(buyerWallet);
        given(walletSupport.getSystemWallet()).willReturn(systemWallet);
    }

    private void verifyHoldingLogged(String amount) {
        verify(cashLogSupport, times(1)).recordHoldingLog(
                eq(buyerWallet),
                eq(systemWallet),
                eq(bd(amount)),
                eq(relType),
                eq(relId)
        );
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
