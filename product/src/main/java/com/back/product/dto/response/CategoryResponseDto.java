package com.back.product.dto.response;

import com.back.product.dto.CategoryDto;
import lombok.Builder;

@Builder
public record CategoryResponseDto(
        CategoryDto category
) {
}
