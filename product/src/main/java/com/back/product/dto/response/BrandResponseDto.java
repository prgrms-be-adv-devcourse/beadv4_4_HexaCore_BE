package com.back.product.dto.response;

import com.back.product.dto.model.BrandDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BrandResponseDto(
        @NotNull(message = "브랜드 정보는 필수입니다.")
        @Valid
        BrandDto brand
) {
}
