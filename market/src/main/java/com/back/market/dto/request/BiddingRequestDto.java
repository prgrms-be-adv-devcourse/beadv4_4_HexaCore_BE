package com.back.market.dto.request;

import java.math.BigDecimal;

/**
 * Controller에서 Facade로, Facade에서 UseCase로 데이터를 넘길 때 사용
 */
public record BiddingRequestDto(
        Long productId,
        BigDecimal price,
        String size
) {
    public static BiddingRequestDto of(Long productId, BigDecimal price, String size) {
        return new BiddingRequestDto(productId, price, size);
    }
}
