package com.back.market.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class InvalidBiddingPriceException extends CustomException {
    public InvalidBiddingPriceException(FailureCode failureCode) {
        super(failureCode);
    }
}
