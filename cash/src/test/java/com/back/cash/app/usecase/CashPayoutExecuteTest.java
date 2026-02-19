package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PayoutRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payout;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PayoutStatus;
import com.back.cash.domain.enums.WalletType;
import com.back.cash.domain.event.CashPayoutRequestedCommand;
import com.back.cash.mapper.PayoutMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashPayoutExecuteTest {

    @Mock
    PayoutRepository payoutRepository;

    @Mock
    WalletSupport walletSupport;

    @Mock
    CashLogSupport cashLogSupport;

    @InjectMocks
    CashPayoutUseCase cashPayoutUseCase;

    private Wallet payeeWallet;
    private Wallet systemWallet;

    private static final Long SETTLEMENT_ID = 1L;
    private static final Long PAYEE_ID = 10L;
    private static final BigDecimal GROSS = new BigDecimal("10000");
    private static final BigDecimal NET = new BigDecimal("9000");
    private static final BigDecimal FEE = new BigDecimal("1000");

    @BeforeEach
    void setUp() {
        payeeWallet = Wallet.builder()
                .id(1L)
                .userId(PAYEE_ID)
                .balance(new BigDecimal("5000"))
                .walletType(WalletType.USER)
                .build();

        systemWallet = Wallet.builder()
                .id(2L)
                .userId(0L)
                .balance(new BigDecimal("100000"))
                .walletType(WalletType.SYSTEM)
                .build();
    }

    @Test
    @DisplayName("정상 정산 시 판매자 지갑에 netAmount가 입금된다")
    void execute_depositsNetAmountToPayeeWallet() {
        // given
        CashPayoutRequestedCommand event = event();
        when(payoutRepository.existsBySettlementId(SETTLEMENT_ID)).thenReturn(false);
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletSupport.getUserWallet(PAYEE_ID)).thenReturn(payeeWallet);
        when(walletSupport.getSystemWallet()).thenReturn(systemWallet);

        // when
        cashPayoutUseCase.execute(event);

        // then
        assertThat(payeeWallet.getBalance()).isEqualByComparingTo("14000"); // 5000 + 9000
    }

    @Test
    @DisplayName("정상 정산 시 시스템 지갑에 feeAmount가 입금된다")
    void execute_depositsFeeAmountToSystemWallet() {
        // given
        CashPayoutRequestedCommand event = event();
        when(payoutRepository.existsBySettlementId(SETTLEMENT_ID)).thenReturn(false);
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletSupport.getUserWallet(PAYEE_ID)).thenReturn(payeeWallet);
        when(walletSupport.getSystemWallet()).thenReturn(systemWallet);

        // when
        cashPayoutUseCase.execute(event);

        // then
        assertThat(systemWallet.getBalance()).isEqualByComparingTo("101000"); // 100000 + 1000
    }

    @Test
    @DisplayName("정상 정산 시 판매자/시스템 CashLog가 각각 기록된다")
    void execute_recordsCashLogs() {
        // given
        CashPayoutRequestedCommand event = event();
        when(payoutRepository.existsBySettlementId(SETTLEMENT_ID)).thenReturn(false);
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletSupport.getUserWallet(PAYEE_ID)).thenReturn(payeeWallet);
        when(walletSupport.getSystemWallet()).thenReturn(systemWallet);

        // when
        cashPayoutUseCase.execute(event);

        // then
        verify(cashLogSupport).recordSettlementPayoutLog(payeeWallet, NET, SETTLEMENT_ID);
        verify(cashLogSupport).recordSystemSettlementPayoutLog(systemWallet, FEE, SETTLEMENT_ID);
    }

    @Test
    @DisplayName("정상 정산 시 Payout 상태가 DONE으로 변경된다")
    void execute_marksPayoutDone() {
        // given
        CashPayoutRequestedCommand event = event();
        when(payoutRepository.existsBySettlementId(SETTLEMENT_ID)).thenReturn(false);

        ArgumentCaptor<Payout> captor = ArgumentCaptor.forClass(Payout.class);
        when(payoutRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletSupport.getUserWallet(PAYEE_ID)).thenReturn(payeeWallet);
        when(walletSupport.getSystemWallet()).thenReturn(systemWallet);

        // when
        cashPayoutUseCase.execute(event);

        // then
        Payout saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(PayoutStatus.DONE);
        assertThat(saved.getCompletedAt()).isNotNull();
    }

    private CashPayoutRequestedCommand event() {
        return new CashPayoutRequestedCommand(SETTLEMENT_ID, PAYEE_ID, GROSS, NET, FEE);
    }
}
