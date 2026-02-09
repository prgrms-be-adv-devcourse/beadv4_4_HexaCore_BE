package com.back.product.dto.model;

import lombok.Builder;

@Builder
public record ProductOptionDto(
        Long productOptionValueId,
        String groupName,
        String value
) {
}
