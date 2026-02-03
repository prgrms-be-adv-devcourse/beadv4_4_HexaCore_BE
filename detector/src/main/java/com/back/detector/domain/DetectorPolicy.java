package com.back.detector.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DetectorPolicy {
    BID_SPAM(5, 1, "반복적인 입찰 시도");           // 입찰 스팸: 5회, 1분
    private final int maxAttempts;
    private final int timeWindowMinutes;
    private final String banReason;
}
