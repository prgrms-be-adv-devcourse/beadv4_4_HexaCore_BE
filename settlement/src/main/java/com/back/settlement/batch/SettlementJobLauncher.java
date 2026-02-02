package com.back.settlement.batch;

import static com.back.common.code.FailureCode.BATCH_EXECUTION_FAILED;
import static com.back.common.code.FailureCode.SETTLEMENT_ALREADY_PROCESSED_DAILY;
import static com.back.common.code.FailureCode.SETTLEMENT_ALREADY_PROCESSED_MONTHLY;

import com.back.common.exception.ConflictException;
import com.back.common.exception.CustomException;
import com.back.settlement.app.dto.response.BatchExecutionResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementJobLauncher {
    private final JobLauncher jobLauncher;
    private final Job dailySettlementJob;
    private final Job monthlySettlementJob;

    public BatchExecutionResponse runDaily(LocalDate targetDate) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", targetDate.toString())
                    .toJobParameters();
            log.info("일간 정산 배치 Job 시작. targetDate={}", targetDate);

            JobExecution execution = jobLauncher.run(dailySettlementJob, jobParameters);
            log.info("일간 정산 배치 Job 완료. targetDate={}, status={}", targetDate, execution.getStatus());

            return BatchExecutionResponse.from(execution, targetDate.toString());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("일간 정산 배치 이미 완료됨. targetDate={}", targetDate);
            throw new ConflictException(SETTLEMENT_ALREADY_PROCESSED_DAILY);
        } catch (Exception e) {
            log.error("일간 정산 배치 Job 실행 실패. targetDate={}", targetDate, e);
            throw new CustomException(e.getMessage(), BATCH_EXECUTION_FAILED);
        }
    }

    public BatchExecutionResponse runMonthly(YearMonth targetMonth) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetMonth", targetMonth.toString())
                    .toJobParameters();
            log.info("월간 정산 배치 Job 시작. targetMonth={}", targetMonth);

            JobExecution execution = jobLauncher.run(monthlySettlementJob, jobParameters);
            log.info("월간 정산 배치 Job 완료. targetMonth={}, status={}", targetMonth, execution.getStatus());

            return BatchExecutionResponse.from(execution, targetMonth.toString());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("월간 정산 배치 이미 완료됨. targetMonth={}", targetMonth);
            throw new ConflictException(SETTLEMENT_ALREADY_PROCESSED_MONTHLY);
        } catch (Exception e) {
            log.error("월간 정산 배치 Job 실행 실패. targetMonth={}", targetMonth, e);
            throw new CustomException(e.getMessage(), BATCH_EXECUTION_FAILED);
        }
    }
}
