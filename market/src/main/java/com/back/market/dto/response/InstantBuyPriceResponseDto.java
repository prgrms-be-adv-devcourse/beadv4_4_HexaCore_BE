package com.back.market.dto.response;

import java.math.BigDecimal;

/**
 * 특정 상품의 즉시 구매가를 조회하기 위한 DTO
 * @param productId 상품ID(PK)
 * @param buyNowPrice 즉시 구매가
 */
public record InstantBuyPriceResponseDto(
        Long productId,          // 상품ID
        BigDecimal buyNowPrice  // 즉시 구매가(=판매 입찰가 중 최저가)
) {
    // 팩토리 메서드
    public static InstantBuyPriceResponseDto of(Long productId, BigDecimal buyNowPrice) {
        return new InstantBuyPriceResponseDto(productId, buyNowPrice);
    }
}
