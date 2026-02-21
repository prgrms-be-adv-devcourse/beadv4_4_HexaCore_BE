package com.back.detector.mapper;

import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.BidSpamLog;
import com.back.detector.domain.DetectorPolicy;
import org.springframework.stereotype.Component;

@Component
public class BidSpamLogMapper {

    public BidSpamLog toBidSpamLog(Long userId, BidSpamBanLevel banLevel, Long requestCount) {
        return BidSpamLog.builder()
                .userId(userId)
                .requestCount(requestCount)
                .timeWindowMinutes(DetectorPolicy.BID_SPAM.getTimeWindowMinutes())
                .banLevel(banLevel)
                .build();
    }
}
