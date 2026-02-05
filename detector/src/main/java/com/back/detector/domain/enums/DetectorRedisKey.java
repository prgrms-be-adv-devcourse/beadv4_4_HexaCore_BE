package com.back.detector.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DetectorRedisKey {
    BID_COUNT("bid:count:"),
    CRAWLING_COUNT("crawl:count:"),
    CRAWLING_BAN("crawl:ban:"),
    TRUSTED_IP("trusted:ips:"),
    NEW_IP_TIMESTAMP("new:ip:timestamp:"),
    NEW_IP_AMOUNT("new:ip:amount:"),
    USER_BLOCKED("user:blocked:");

    private final String prefix;

    public String getKey(Long userId) {
        return prefix + userId;
    }

    public String getKey(String ip) {
        return prefix + ip;
    }

    public String getKey(Long userId, String ip) {
        return prefix + userId + ":" + ip;
    }
}
