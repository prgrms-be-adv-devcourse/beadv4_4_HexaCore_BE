package com.back.common.exception;

import com.back.common.code.FailureCode;
import com.back.common.response.CommonResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        final BindingResult bindingResult = ex.getBindingResult();
        final Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()); // ← 여기
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.createValidationError(bindingResult));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<?>> handleCustomException(CustomException ex) {
        log.error(">>> handle: CustomException ", ex);
        FailureCode failureCode = ex.getFailureCode();
        return ResponseEntity
                .status(failureCode.getHttpStatus())
                .body(CommonResponse.createError(failureCode.getHttpStatus(), failureCode.getCode(), failureCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleException(Exception ex) {
        log.error(">>> handle: Exception ", ex);
        FailureCode failureCode = FailureCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(failureCode.getHttpStatus())
                .body(CommonResponse.createError(failureCode.getHttpStatus(), failureCode.getCode(), failureCode.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.error(">>> handle: HttpRequestMethodNotSupportedException ", ex);
        FailureCode failureCode = FailureCode.METHOD_NOT_ALLOWED;
        return ResponseEntity
                .status(failureCode.getHttpStatus())
                .body(CommonResponse.createError(failureCode.getHttpStatus(), failureCode.getCode(), failureCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        FailureCode failureCode = FailureCode.INVALID_TYPE_VALUE;
        Map<String, String> errors = new HashMap<>();
        errors.put(e.getName(), e.getValue() == null ? "" : e.getValue().toString());
        return ResponseEntity
                .status(failureCode.getHttpStatus())
                .body(CommonResponse.createError(
                        failureCode.getHttpStatus(),
                        failureCode.getCode(),
                        failureCode.getMessage(),
                        errors
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<?>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("handle: HttpMessageNotReadableException ", ex);
        FailureCode errorCode = FailureCode.INVALID_REQUEST;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.createError(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<?>> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.error("handle: MissingServletRequestParameterException ", ex);
        FailureCode errorCode = FailureCode.MISSING_REQUIRED_FIELD;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.createError(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("handle: ConstraintViolationException ", ex);
        FailureCode errorCode = FailureCode.INVALID_INPUT_VALUE;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.createError(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage()));
    }
}
