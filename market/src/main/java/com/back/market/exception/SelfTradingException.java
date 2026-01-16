package com.back.market.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class SelfTradingException extends CustomException {
    public SelfTradingException(FailureCode failureCode) {
        super(failureCode);
    }
}
