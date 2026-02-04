package com.back.detector.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DetectorRedisKey {
    BID_COUNT("bid:count:"),
    CRAWLING_COUNT("crawl:count:"),
    CRAWLING_BAN("crawl:ban:"),
    USER_IP("ip:");

    private final String prefix;

    public String getKey(Long userId) {
        return prefix + userId;
    }

    public String getKey(String ip) {
        return prefix + ip;
    }
}
