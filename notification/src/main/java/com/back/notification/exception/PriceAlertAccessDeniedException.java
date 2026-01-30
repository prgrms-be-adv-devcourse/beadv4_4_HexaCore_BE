package com.back.notification.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class PriceAlertAccessDeniedException extends CustomException {
    public PriceAlertAccessDeniedException() {
        super("해당 가격 알림에 접근할 수 없습니다.", FailureCode.FORBIDDEN);
    }
}
