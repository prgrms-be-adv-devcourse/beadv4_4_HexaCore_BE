package com.back.common.exception;

import com.back.common.code.FailureCode;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException() {
        super(FailureCode.UNAUTHORIZED);
    }

    public UnauthorizedException(FailureCode failureCode) {
        super(failureCode);
    }
}
