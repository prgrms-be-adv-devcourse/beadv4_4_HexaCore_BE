package com.back.detector.domain;

public record CrawlingDetectResult(
        CrawlingBanLevel banLevel,
        int requestCount
) {
}
