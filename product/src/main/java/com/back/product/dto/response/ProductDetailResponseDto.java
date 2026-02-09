package com.back.product.dto.response;

import com.back.product.dto.model.ProductDetailDto;
import lombok.Builder;

@Builder
public record ProductDetailResponseDto(
        ProductDetailDto product
) {
}
