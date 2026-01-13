package com.back.common.exception;

import com.back.common.code.FailureCode;

public class ForbiddenException extends CustomException {
    public ForbiddenException() {
        super(FailureCode.FORBIDDEN);
    }

    public ForbiddenException(FailureCode failureCode) {
        super(failureCode);
    }
}
