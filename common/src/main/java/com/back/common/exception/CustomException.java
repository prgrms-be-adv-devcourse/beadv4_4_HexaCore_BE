package com.back.common.exception;

import com.back.common.code.FailureCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final FailureCode failureCode;

    public CustomException(FailureCode failureCode) {
        super(failureCode.getMessage());
        this.failureCode = failureCode;
    }

    public CustomException(String message, FailureCode errorCode) {
        super(message);
        this.failureCode = errorCode;
    }
}
