package com.back.product.dto.response;

import com.back.product.dto.CategoryDto;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryListResponseDto(
        List<CategoryDto> categories
) {
}
