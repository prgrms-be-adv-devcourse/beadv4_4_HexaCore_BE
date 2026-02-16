package com.back.product.event.spring;

import com.back.product.dto.model.BrandDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record BrandCreationCompletedEvent(
        @NotNull(message = "브랜드 DTO는 필수입니다.")
        @Valid
        List<BrandDto> brands
) {
}
