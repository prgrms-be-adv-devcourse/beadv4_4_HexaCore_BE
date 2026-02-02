package com.back.settlement.config.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.back.settlement.batch.SettlementJobLauncher;
import com.back.settlement.batch.SettlementScheduler;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementScheduler 테스트")
class SettlementSchedulerTest {

    @InjectMocks
    private SettlementScheduler settlementScheduler;

    @Mock
    private SettlementJobLauncher settlementJobLauncher;

    @Test
    @DisplayName("전 월을 대상으로 JobLauncher를 호출한다")
    void runMonthlySettlement_LaunchesJobWithPreviousMonth() {
        // when
        settlementScheduler.runMonthlySettlement();

        // then
        ArgumentCaptor<YearMonth> captor = ArgumentCaptor.forClass(YearMonth.class);
        verify(settlementJobLauncher).runMonthly(captor.capture());

        YearMonth targetMonth = captor.getValue();
        assertThat(targetMonth).isEqualTo(YearMonth.now().minusMonths(1));
    }

    @Test
    @DisplayName("전 일을 대상으로 JobLauncher를 호출한다")
    void runDailySettlement_LaunchesJobWithPreviousDaily() {
        // when
        settlementScheduler.runDailySettlement();

        // then
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(settlementJobLauncher).runDaily(captor.capture());

        assertThat(captor.getValue()).isEqualTo(LocalDate.now().minusDays(1));
    }

}