package com.back.common.exception;

import com.back.common.code.FailureCode;

public class InvalidValueException extends CustomException {
    public InvalidValueException() {
        super(FailureCode.BAD_REQUEST);
    }

    public InvalidValueException(FailureCode failureCode) {
        super(failureCode);
    }
}