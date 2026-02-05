package com.back.detector.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class HijackDetectedException extends CustomException {
    public HijackDetectedException() {
        super(DetectorFailureCode.HIJACK_DETECTED.getMessage(), FailureCode.FORBIDDEN);
    }
}
