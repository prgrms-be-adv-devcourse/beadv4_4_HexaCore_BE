package com.back.settlement.batch;

import com.back.settlement.app.dto.response.BatchExecutionResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementJobHistoryLauncher {
    private static final String DAILY_JOB_NAME = "dailySettlementJob";
    private static final String MONTHLY_JOB_NAME = "monthlySettlementJob";

    private final JobExplorer jobExplorer;

    public List<BatchExecutionResponse> getHistory(String jobType, int count) {
        String jobName = resolveJobName(jobType);

        List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, count);
        return jobInstances.stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .sorted((e1, e2) -> e2.getStartTime().compareTo(e1.getStartTime()))
                .limit(count)
                .map(execution -> {
                    String targetDate = extractTargetDate(execution, jobType);
                    return BatchExecutionResponse.from(execution, targetDate);
                })
                .toList();
    }

    public List<BatchExecutionResponse> getAllHistory(int count) {
        List<BatchExecutionResponse> dailyHistory = getHistory("daily", count);
        List<BatchExecutionResponse> monthlyHistory = getHistory("monthly", count);

        return java.util.stream.Stream.concat(dailyHistory.stream(), monthlyHistory.stream())
                .sorted((e1, e2) -> e2.startTime().compareTo(e1.startTime()))
                .limit(count)
                .toList();
    }

    private String resolveJobName(String jobType) {
        return switch (jobType.toLowerCase()) {
            case "daily" -> DAILY_JOB_NAME;
            case "monthly" -> MONTHLY_JOB_NAME;
            default -> throw new IllegalArgumentException("지원하지 않는 배치 타입: " + jobType);
        };
    }

    private String extractTargetDate(JobExecution execution, String jobType) {
        JobParameters params = execution.getJobParameters();
        return switch (jobType.toLowerCase()) {
            case "daily" -> params.getString("targetDate");
            case "monthly" -> params.getString("targetMonth");
            default -> "";
        };
    }
}
