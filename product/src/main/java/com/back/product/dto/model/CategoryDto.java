package com.back.product.dto.model;

import lombok.Builder;

@Builder
public record CategoryDto(
        Long categoryId,
        String name,
        String imageUrl
) {
}
