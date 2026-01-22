package com.back.product.dto;

import lombok.Builder;

@Builder
public record ProductOptionDto(
        Long productOptionValueId,
        String groupName,
        String value
) {
}
