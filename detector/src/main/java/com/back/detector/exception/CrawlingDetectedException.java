package com.back.detector.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class CrawlingDetectedException extends CustomException {
    public CrawlingDetectedException() {
        super(DetectorFailureCode.CRAWLING_DETECTED.getMessage(), FailureCode.TOO_MANY_REQUESTS);
    }
}
