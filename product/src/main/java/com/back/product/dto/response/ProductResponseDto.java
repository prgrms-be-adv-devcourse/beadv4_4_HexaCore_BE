package com.back.product.dto.response;

import com.back.product.dto.model.ProductDto;
import com.back.product.dto.model.ProductInfoDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductResponseDto(
        ProductInfoDto productInfo,
        List<ProductDto> products
) {
}
