package com.back.detector.dto.response;

import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.BidSpamLog;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record BidSpamLogResponse(
        Long id,
        Long userId,
        long requestCount,
        long timeWindowMinutes,
        BidSpamBanLevel banLevel,
        LocalDateTime createdAt
) {
    public static BidSpamLogResponse from(BidSpamLog log) {
        return BidSpamLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .requestCount(log.getRequestCount())
                .timeWindowMinutes(log.getTimeWindowMinutes())
                .banLevel(log.getBanLevel())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
