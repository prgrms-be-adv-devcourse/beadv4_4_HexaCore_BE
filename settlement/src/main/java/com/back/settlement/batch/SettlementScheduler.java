package com.back.settlement.batch;

import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    private final SettlementJobLauncher settlementJobLauncher;

    @Scheduled(cron = "0 0 2 * * *")  // 매일 02:00 실행
    public void runDailySettlement() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        log.info("일간 정산 스케줄 실행. targetDate={}", targetDate);
        try {
            settlementJobLauncher.runDaily(targetDate);
        } catch (Exception e) {
            // TODO: 운영 단계에서 알림을 Slack 전송
            log.error("일간 정산 스케줄 실행 실패. targetDate={}", targetDate, e);
        }
    }

    @Scheduled(cron = "0 0 2 1 * *")  // 매월 1일 02:00 실행
    public void runMonthlySettlement() {
        YearMonth targetMonth = YearMonth.now().minusMonths(1);
        log.info("월간 정산 스케줄 실행. targetMonth={}", targetMonth);
        try {
            settlementJobLauncher.runMonthly(targetMonth);
        } catch (Exception e) {
            // TODO: 운영 단계에서 알림을 Slack 전송
            log.error("월간 정산 스케줄 실행 실패. targetMonth={}", targetMonth, e);
        }
    }
}
