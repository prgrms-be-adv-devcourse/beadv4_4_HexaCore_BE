package com.back.detector.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class BidSpamException extends CustomException {
    public BidSpamException() {
        super(DetectorFailureCode.BID_SPAM.getMessage(), FailureCode.BAD_REQUEST);
    }
}
