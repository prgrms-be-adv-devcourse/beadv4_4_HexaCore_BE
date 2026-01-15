package com.back.market.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 거래 관련(Market) 에러 코드 정의한 Enum
 */
@Getter
@AllArgsConstructor
public enum MarketErrorCode {
    PRODUCT_NOT_FOUND("존재하지 않는 상품입니다."),
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),

    // 입찰 관련
    INVALID_PRICE_UNIT("입찰 가격은 1,000원 단위여야 합니다."),
    INVALID_BID_PRICE_BUY("구매 입찰가는 즉시 구매가보다 낮아야 합니다."),
    INVALID_BID_PRICE_SELL("판매 입찰가는 즉시 판매가보다 높아야 합니다.");

    private final String message;
}
