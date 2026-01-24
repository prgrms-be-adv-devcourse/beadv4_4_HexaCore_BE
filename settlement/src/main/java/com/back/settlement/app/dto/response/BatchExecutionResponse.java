package com.back.settlement.app.dto.response;

import java.time.LocalDateTime;

public record BatchExecutionResponse(
        Long jobId,
        String status,
        String targetMonth,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int processedCount
) {}