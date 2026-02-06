package com.back.common.product.event.payload;

public record CategoryPayload(
        Long categoryId,
        String name
) {
}
