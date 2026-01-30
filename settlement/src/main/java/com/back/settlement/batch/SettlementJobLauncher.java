package com.back.settlement.batch;

import com.back.settlement.app.dto.response.BatchExecutionResponse;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

/**
 * 정산 배치 Job을 실행하는 Launcher 클래스입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementJobLauncher {
    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    public BatchExecutionResponse run(YearMonth targetMonth) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetMonth", targetMonth.toString())
                    .toJobParameters();
            log.info("정산 배치 Job 시작. targetMonth={}", targetMonth);

            JobExecution execution = jobLauncher.run(settlementJob, jobParameters);
            log.info("정산 배치 Job 완료. targetMonth={}, status={}", targetMonth, execution.getStatus());

            return toBatchExecutionResponse(execution, targetMonth);
        } catch (Exception e) {
            log.error("정산 배치 Job 실행 실패. targetMonth={}", targetMonth, e);
            throw new RuntimeException("정산 배치 실행 실패", e);
        }
    }

    private BatchExecutionResponse toBatchExecutionResponse(JobExecution execution, YearMonth targetMonth) {
        int processedCount = execution.getStepExecutions().stream()
                .mapToInt(step -> (int) step.getWriteCount())
                .sum();

        return new BatchExecutionResponse(
                execution.getId(),
                execution.getStatus().toString(),
                targetMonth.toString(),
                execution.getStartTime(),
                execution.getEndTime(),
                processedCount
        );
    }
}