package com.back.detector.mapper;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.CrawlingLog;
import com.back.detector.domain.DetectorPolicy;
import org.springframework.stereotype.Component;

@Component
public class CrawlingLogMapper {

    public CrawlingLog toCrawlingLog(String ip, CrawlingBanLevel banLevel) {
        return CrawlingLog.builder()
                .ipAddress(ip)
                .requestCount(DetectorPolicy.CRAWLING.getMaxAttempts())
                .timeWindowMinutes(DetectorPolicy.CRAWLING.getTimeWindowMinutes())
                .banLevel(banLevel)
                .build();
    }
}
