package com.back.common.code;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode {
    /**
     * 200 OK SUCCESS
     */
    OK(HttpStatus.OK, "OK", "요청이 성공했습니다."),

    /**
     * 201 CREATED SUCCESS
     */
    CREATED(HttpStatus.CREATED, "CREATED", "요청이 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
