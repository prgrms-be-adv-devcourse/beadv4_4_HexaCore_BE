package com.back.product.dto.response;

import com.back.product.dto.model.CategoryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryListResponseDto(
        @NotEmpty(message = "카테고리 목록은 비어 있을 수 없습니다.")
        @Valid
        List<CategoryDto> categories
) {
}
