package com.back.product.dto.command;

import lombok.Builder;

@Builder
public record BrandDataCommand(
        String name,
        String imageUrl
) {
}
