package com.back.detector.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DetectorRedisKey {
    BID_COUNT("bid:count:"),
    BID_BAN("bid:ban:"),
    BID_BAN_COUNT("bid:ban:count:"),
    CRAWLING_COUNT("crawl:count:"),
    CRAWLING_BAN("crawl:ban:"),
    CRAWLING_BAN_COUNT("crawl:ban:count:"),
    TRUSTED_IP("trusted:ips:"),
    NEW_IP_TIMESTAMP("new:ip:timestamp:"),
    NEW_IP_AMOUNT("new:ip:amount:");

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
