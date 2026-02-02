package com.back.settlement.app.dto.response;

import java.time.LocalDateTime;
import org.springframework.batch.core.job.JobExecution;

public record BatchExecutionResponse(
        Long jobId,
        String jobName,
        String status,
        String targetDate,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int processedCount
) {
    public static BatchExecutionResponse from(JobExecution execution, String targetDate) {
        int processedCount = execution.getStepExecutions().stream()
                .mapToInt(step -> (int) step.getWriteCount())
                .sum();

        return new BatchExecutionResponse(
                execution.getId(),
                execution.getJobInstance().getJobName(),
                execution.getStatus().toString(),
                targetDate,
                execution.getStartTime(),
                execution.getEndTime(),
                processedCount
        );
    }
}