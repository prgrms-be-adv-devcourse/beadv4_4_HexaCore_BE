package com.back.product.dto.event.kafka;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
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
