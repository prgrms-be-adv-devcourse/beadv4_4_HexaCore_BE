package com.back.common.product.event.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductInfoPayload(
        Long productInfoId,
        BrandPayload brand,
        CategoryPayload category,
        String name,
        String code,
        BigDecimal releasePrice,
        LocalDateTime releaseDate
) {
}
