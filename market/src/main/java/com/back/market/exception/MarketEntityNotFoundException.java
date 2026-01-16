package com.back.market.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class MarketEntityNotFoundException extends CustomException {
    public MarketEntityNotFoundException(FailureCode failureCode) {
        super(failureCode);
    }
}
