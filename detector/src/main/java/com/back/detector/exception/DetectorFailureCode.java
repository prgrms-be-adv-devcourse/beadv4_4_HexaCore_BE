package com.back.detector.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DetectorFailureCode {
    BID_SPAM(HttpStatus.BAD_REQUEST, "BID_SPAM", "입찰 횟수를 초과하였습니다."),
    CRAWLING_DETECTED(HttpStatus.TOO_MANY_REQUESTS, "CRAWLING_DETECTED", "크롤링 봇이 감지되었습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
