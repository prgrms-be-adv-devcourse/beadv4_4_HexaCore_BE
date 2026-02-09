package com.back.product.dto.response;

import com.back.product.dto.model.ProductDetailDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailListResponseDto(
        List<ProductDetailDto> products
) {
}
