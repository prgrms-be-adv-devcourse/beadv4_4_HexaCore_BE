package com.back.market.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class BidSpamDetectedException extends CustomException {
    public BidSpamDetectedException() {
        super("입찰 스팸이 감지되어 입찰이 제한됩니다.", FailureCode.BAD_REQUEST);
    }
}
