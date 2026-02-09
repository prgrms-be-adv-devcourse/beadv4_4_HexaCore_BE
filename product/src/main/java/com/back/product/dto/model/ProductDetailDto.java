package com.back.product.dto.model;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailDto(
        ProductInfoDto productInfo,
        List<ProductDto> products
) {
}
