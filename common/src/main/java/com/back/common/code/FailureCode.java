package com.back.common.code;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FailureCode {
    /**
     * 400 Bad Request
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "필수 입력값이 누락되었습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청이 올바르지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력값이 유효하지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "INVALID_TYPE_VALUE", "필드가 잘못되었습니다."),

    // Market 모듈에서 사용
    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "PRODUCT_NOT_FOUND","존재하지 않는 상품입니다."),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND","존재하지 않는 사용자입니다."),
    INVALID_PRICE_UNIT(HttpStatus.BAD_REQUEST, "INVALID_PRICE_UNIT","입찰 가격은 1,000원 단위여야 합니다."),
    INVALID_BID_PRICE_BUY(HttpStatus.BAD_REQUEST, "INVALID_BID_PRICE_BUY","구매 입찰가는 즉시 구매가보다 낮아야 합니다."),
    INVALID_BID_PRICE_SELL(HttpStatus.BAD_REQUEST, "INVALID_BID_PRICE_SELL","판매 입찰가는 즉시 판매가보다 높아야 합니다."),
    SELF_TRADING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SELF_TRADING_NOT_ALLOWED", "본인의 입찰 상품과는 거래할 수 없습니다."),

    /**
     * 401 Unauthorized
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "TOKEN_MISSING", "토큰이 존재하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "토큰이 유효하지 않습니다."),
    TOKEN_CATEGORY_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_CATEGORY_INVALID", "토큰 유형이 올바르지 않습니다."),


    /**
     * 403 Forbidden
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    SETTLEMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SETTLEMENT_ACCESS_DENIED", "해당 정산 내역에 접근 권한이 없습니다."),

    /**
     * 404 Not Found
     */
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 자원이 존재하지 않습니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "대상을 찾을 수 없습니다."),
    SETTLEMENT_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_ITEM_NOT_FOUND", "정산 상품을 찾을 수 없습니다."),

    /**
     * 405 Method Not Allowed
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다."),


    /**
     * 409 Conflict
     */
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "이미 존재하는 리소스입니다."),
    BRAND_NAME_DUPLICATE(HttpStatus.CONFLICT, "BRAND_NAME_DUPLICATE", "이미 존재하는 브랜드 이름입니다."),

    /**
     * 500 Internal Server Error
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
