package com.back.product.dto.response;

import com.back.product.dto.model.BrandDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record BrandListResponseDto(
        @NotEmpty(message = "브랜드 목록은 비어 있을 수 없습니다.")
        @Valid
        List<BrandDto> brands
) {
}
