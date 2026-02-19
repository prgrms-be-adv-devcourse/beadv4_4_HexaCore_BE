package com.back.detector.dto.response;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.CrawlingLog;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CrawlingLogResponse(
        Long id,
        String ipAddress,
        int requestCount,
        int timeWindowMinutes,
        CrawlingBanLevel banLevel,
        LocalDateTime createdAt
) {
    public static CrawlingLogResponse from(CrawlingLog log) {
        return CrawlingLogResponse.builder()
                .id(log.getId())
                .ipAddress(log.getIpAddress())
                .requestCount(log.getRequestCount())
                .timeWindowMinutes(log.getTimeWindowMinutes())
                .banLevel(log.getBanLevel())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
