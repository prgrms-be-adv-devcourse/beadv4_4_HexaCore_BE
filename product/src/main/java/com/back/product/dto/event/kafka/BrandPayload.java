package com.back.product.dto.event.kafka;

import lombok.Builder;

@Builder
public record BrandPayload(
        Long brandId,
        String name
) {
}
