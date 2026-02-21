
package com.back.detector.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BidSpamBanLevel {
    FIRST(5),
    SECOND(60);

    private final int banMinutes;

    public static BidSpamBanLevel of(long banCount) {
        if (banCount <= 1) return FIRST;
        return SECOND;
    }
}
