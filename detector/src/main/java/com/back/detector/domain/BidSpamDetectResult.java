package com.back.detector.domain;

public record BidSpamDetectResult(
        BidSpamBanLevel banLevel,
        Long requestCount
) {
}
