package com.back.cash.adapter.in.listener.exception;

public class PayoutMessageParseException extends RuntimeException {
    public PayoutMessageParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
