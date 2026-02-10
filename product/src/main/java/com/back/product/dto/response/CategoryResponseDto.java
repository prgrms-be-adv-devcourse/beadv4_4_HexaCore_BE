package com.back.product.dto.response;

import com.back.product.dto.model.CategoryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CategoryResponseDto(
        @NotNull(message = "카테고리 정보는 필수입니다.")
        @Valid
        CategoryDto category
) {
}
