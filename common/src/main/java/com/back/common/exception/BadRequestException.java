package com.back.common.exception;

import com.back.common.code.FailureCode;

public class BadRequestException extends CustomException {
    public BadRequestException() {
        super(FailureCode.BAD_REQUEST);
    }

    public BadRequestException(FailureCode failureCode) {
        super(failureCode);
    }
}
