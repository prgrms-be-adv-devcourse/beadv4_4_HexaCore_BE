package com.back.product.dto;

import lombok.Builder;

@Builder
public record CategoryDto(
        String name,
        String imageUrl
) {
}
