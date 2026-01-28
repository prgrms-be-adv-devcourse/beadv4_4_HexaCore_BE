package com.back.cash;

import com.back.cash.adapter.out.CashLogRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.domain.CashLog;
import com.back.cash.domain.Wallet;
import com.back.common.dto.cash.enums.RelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CashLogSupportTest {

    @InjectMocks
    private CashLogSupport cashLogSupport;

    @Mock
    private CashLogRepository cashLogRepository;

    @Test
    @DisplayName("로그 기록 시 내부에서 생성된 객체의 금액과 지갑이 정확해야 한다")
    void recordHoldingLog_Test() {
        // given
        Wallet buyer = Wallet.builder().id(1L).balance(new BigDecimal("10000")).build();
        Wallet system = Wallet.builder().id(2L).balance(BigDecimal.ZERO).build();
        BigDecimal amount = new BigDecimal("5000");

        ArgumentCaptor<CashLog> logCaptor = ArgumentCaptor.forClass(CashLog.class);

        cashLogSupport.recordHoldingLog(buyer, system, amount, RelType.ORDER, 100L);

        verify(cashLogRepository, times(2)).save(logCaptor.capture());

        List<CashLog> capturedLogs = logCaptor.getAllValues();

        // 첫 번째 로그 검증
        assertThat(capturedLogs.get(0).getAmount()).isEqualByComparingTo("-5000");
        assertThat(capturedLogs.get(0).getWallet()).isEqualTo(buyer);

        // 두 번째 로그 검증
        assertThat(capturedLogs.get(1).getAmount()).isEqualByComparingTo("5000");
        assertThat(capturedLogs.get(1).getWallet()).isEqualTo(system);

        // 공통 값 검증
        assertThat(capturedLogs.get(0).getRelId()).isEqualTo(100L);
    }
}

