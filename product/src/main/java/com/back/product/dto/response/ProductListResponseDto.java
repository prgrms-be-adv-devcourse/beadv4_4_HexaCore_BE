package com.back.product.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductListResponseDto(
        List<ProductResponseDto> products
) {
}
