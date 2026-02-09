package com.back.product.dto.model;

import lombok.Builder;

@Builder
public record ProductOptionValueDto(
        Long productOptionValueId,
        String groupName,
        String value
) {
}
