package com.back.detector.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DetectorFailureCode {
    BID_SPAM(HttpStatus.BAD_REQUEST, "BID_SPAM", "입찰 횟수를 초과하였습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
