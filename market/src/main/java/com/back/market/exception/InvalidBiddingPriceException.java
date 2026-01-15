package com.back.market.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.market.exception.code.MarketErrorCode;

public class InvalidBiddingPriceException extends CustomException {
    public InvalidBiddingPriceException(MarketErrorCode errorCode) {
        super(errorCode.getMessage(), FailureCode.INVALID_INPUT_VALUE);
    }
}
