package com.back.detector.mapper;

import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.BidSpamLog;
import com.back.detector.domain.DetectorPolicy;

public class BidSpamLogMapper {

    public static BidSpamLog toBidSpamLog(Long userId, BidSpamBanLevel banLevel) {
        return BidSpamLog.builder()
                .userId(userId)
                .requestCount(DetectorPolicy.BID_SPAM.getMaxAttempts())
                .timeWindowMinutes(DetectorPolicy.BID_SPAM.getTimeWindowMinutes())
                .banLevel(banLevel)
                .build();
    }
}
