package com.back.common.exception;

import com.back.common.code.FailureCode;

public class ConflictException extends CustomException {
    public ConflictException() {
        super(FailureCode.BAD_REQUEST);
    }

    public ConflictException(FailureCode failureCode) {
        super(failureCode);
    }
}
