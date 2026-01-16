package com.back.common.response;

import com.back.common.code.SuccessCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonResponse<T> {
    private int status;
    private String code;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return new CommonResponse<>(httpStatus.value(), message, data);
    }

    public static CommonResponse<?> successWithMessage(HttpStatus httpStatus, String message) {
        return new CommonResponse<>(httpStatus.value(), message);
    }

    public static <T> CommonResponse<T> successWithData(HttpStatus httpStatus, T data) {
        return new CommonResponse<>(httpStatus.value(), data);
    }

    public static CommonResponse<?> createError(String message) {
        return new CommonResponse<>(message);
    }

    public static CommonResponse<?> createError(HttpStatus httpStatus, String code, String message) {
        return new CommonResponse<>(httpStatus.value(), code, message);
    }

    public static <T> CommonResponse<T> createError(HttpStatus httpStatus, String code, String message, T data) {
        return new CommonResponse<>(httpStatus.value(), code, message, data);
    }

    public static CommonResponse<?> createValidationError(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return new CommonResponse<>(HttpStatus.BAD_REQUEST.value(), "VALIDATION_FAILED", "요청 데이터가 유효하지 않습니다.", fieldErrors);
    }

    public static <T> CommonResponse<T> success(SuccessCode successCode, T data) {
        return new CommonResponse<>(
                successCode.getHttpStatus().value(),
                successCode.getCode(),
                successCode.getMessage(),
                data
        );
    }

    private CommonResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private CommonResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    private CommonResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private CommonResponse(int status, String code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private CommonResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    private CommonResponse(String message) {
        this.message = message;
    }
}
