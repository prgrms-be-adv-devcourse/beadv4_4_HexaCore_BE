package com.back.detector.mapper;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.CrawlingLog;
import com.back.detector.domain.DetectorPolicy;

public class CrawlingLogMapper {

    public static CrawlingLog toCrawlingLog(String ip, CrawlingBanLevel banLevel) {
        return CrawlingLog.builder()
                .ipAddress(ip)
                .requestCount(DetectorPolicy.CRAWLING.getMaxAttempts())
                .timeWindowMinutes(DetectorPolicy.CRAWLING.getTimeWindowMinutes())
                .banLevel(banLevel)
                .build();
    }
}
