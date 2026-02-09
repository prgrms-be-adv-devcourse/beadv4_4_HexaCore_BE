package com.back.product.dto.response;

import com.back.product.dto.model.ProductSearchDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductSearchResponseDto(
        List<ProductSearchDto> products,
        Long totalPages,
        Long totalElements,
        Long currentPage
) {
}
