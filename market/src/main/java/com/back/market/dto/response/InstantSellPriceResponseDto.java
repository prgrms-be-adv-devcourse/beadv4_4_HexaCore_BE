package com.back.market.dto.response;

import java.math.BigDecimal;

/**
 * 특정 상품의 즉시 판매가를 조회하기 위한 DTO
 * @param productId 상품ID(PK)
 * @param sellNowPrice 즉시 판매가
 */
public record InstantSellPriceResponseDto(
        Long productId,          // 상품ID
        BigDecimal sellNowPrice  // 즉시 판매가(=구매 입찰가 중 최고가)
) {
    // 팩토리 메서드
    public static InstantSellPriceResponseDto of(Long productId, BigDecimal sellNowPrice) {
        return new InstantSellPriceResponseDto(productId, sellNowPrice);
    }
}
