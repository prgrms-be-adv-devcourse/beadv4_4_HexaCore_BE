package com.back.settlement.batch;

import static com.back.common.code.FailureCode.BATCH_EXECUTION_FAILED;
import static com.back.common.code.FailureCode.SETTLEMENT_ALREADY_PROCESSED_DAILY;
import static com.back.common.code.FailureCode.SETTLEMENT_ALREADY_PROCESSED_MONTHLY;

import com.back.common.code.FailureCode;
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
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", targetDate.toString())
                .toJobParameters();
        return executeJob(dailySettlementJob, params, targetDate.toString(), SETTLEMENT_ALREADY_PROCESSED_DAILY);
    }

    public BatchExecutionResponse runMonthly(YearMonth targetMonth) {
        JobParameters params = new JobParametersBuilder()
                .addString("targetMonth", targetMonth.toString())
                .toJobParameters();
        return executeJob(monthlySettlementJob, params, targetMonth.toString(), SETTLEMENT_ALREADY_PROCESSED_MONTHLY);
    }

    private BatchExecutionResponse executeJob(
            Job job, JobParameters params, String target, FailureCode alreadyCompleteCode) {
        try {
            log.info("정산 배치 Job 시작. job={}, target={}", job.getName(), target);
            JobExecution execution = jobLauncher.run(job, params);
            log.info("정산 배치 Job 완료. job={}, target={}, status={}", job.getName(), target, execution.getStatus());
            return BatchExecutionResponse.from(execution, target);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("정산 배치 이미 완료됨. job={}, target={}", job.getName(), target);
            throw new ConflictException(alreadyCompleteCode);
        } catch (Exception e) {
            log.error("정산 배치 Job 실행 실패. job={}, target={}", job.getName(), target, e);
            throw new CustomException(e.getMessage(), BATCH_EXECUTION_FAILED);
        }
    }
}
