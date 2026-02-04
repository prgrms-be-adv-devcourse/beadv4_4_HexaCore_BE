package com.back.detector.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DetectorRedisKey {
    BID_COUNT("bid:count:"),
    VIEW_COUNT("view:count:"),
    USER_IP("ip:");

    private final String prefix;

    public String getKey(Long userId) {
        return prefix + userId;
    }
}
