package com.back.notification.exception;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;

public class NotificationUserNotFoundException extends CustomException {
    public NotificationUserNotFoundException(){
        super("NotificationUser가 존재하지 않습니다.", FailureCode.ENTITY_NOT_FOUND);
    }
}
