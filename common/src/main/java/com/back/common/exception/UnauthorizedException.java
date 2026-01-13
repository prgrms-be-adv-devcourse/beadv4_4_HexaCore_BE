package com.back.common.exception;

import com.back.common.code.FailureCode;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException() {
        super(FailureCode.BAD_REQUEST);
    }

    public UnauthorizedException(FailureCode failureCode) {
        super(failureCode);
    }
}
