package com.back.market.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.market.exception.code.MarketErrorCode;

public class MarketEntityNotFoundException extends CustomException {
    public MarketEntityNotFoundException(MarketErrorCode errorCode) {
        super(errorCode.getMessage(), FailureCode.ENTITY_NOT_FOUND);
    }
}
