package com.back.product.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductDto(
        Long productId,
        Long inventory,
        List<ProductOptionDto> options,
        List<String> imageUrls
) {
}

