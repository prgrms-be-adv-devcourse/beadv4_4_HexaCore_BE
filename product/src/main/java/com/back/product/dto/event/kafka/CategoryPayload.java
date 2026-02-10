package com.back.product.dto.event.kafka;

import lombok.Builder;

@Builder
public record CategoryPayload(
        Long categoryId,
        String name
) {
}
