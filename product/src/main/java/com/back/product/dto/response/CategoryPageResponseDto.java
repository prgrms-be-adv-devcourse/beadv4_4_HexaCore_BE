package com.back.product.dto.response;

import com.back.product.dto.model.CategoryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryPageResponseDto(
        @NotEmpty(message = "카테고리 목록은 비어 있을 수 없습니다.")
        @Valid
        List<CategoryDto> categories,

        @NotNull(message = "총 페이지 수는 필수입니다.")
        @Min(value = 0, message = "총 페이지 수는 0 이상이어야 합니다.")
        Integer totalPages,

        @NotNull(message = "총 요소 수는 필수입니다.")
        @Min(value = 0, message = "총 요소 수는 0 이상이어야 합니다.")
        Long totalElements,

        @NotNull(message = "현재 페이지는 필수입니다.")
        @Min(value = 0, message = "현재 페이지는 0 이상이어야 합니다.")
        Integer currentPage
) {
}
