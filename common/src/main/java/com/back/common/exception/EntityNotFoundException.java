package com.back.common.exception;

import com.back.common.code.FailureCode;

public class EntityNotFoundException extends CustomException {
    public EntityNotFoundException() {
        super(FailureCode.ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(FailureCode failureCode) {
        super(failureCode);
    }
}
