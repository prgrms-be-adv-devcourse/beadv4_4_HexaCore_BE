package com.back.detector.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 크롤링 봇 차단 단계
 * 초과 횟수마다 차단 시간이 단계적으로 증가
 */
@Getter
@RequiredArgsConstructor
public enum CrawlingBanLevel {
    FIRST(5),    // 첫 번째 차단: 5분
    SECOND(60);  // 두 번째 차단 이상: 1시간

    private final int banMinutes;

    /**
     * 현재 차단 횟수(0-base)에 맞는 차단 단계 반환
     * 2회 이상이면 최대 단계(SECOND)로 고정
     */
    public static CrawlingBanLevel of(long banCount) {
        if (banCount <= 1) return FIRST;
        return SECOND;
    }
}
