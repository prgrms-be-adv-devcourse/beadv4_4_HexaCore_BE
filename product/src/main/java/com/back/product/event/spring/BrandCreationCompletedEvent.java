package com.back.product.event.spring;

import com.back.product.dto.model.BrandDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record BrandCreationCompletedEvent(
        @NotEmpty(message = "이벤트 ID는 필수입니다.")
        String eventId,

        @NotNull(message = "브랜드 DTO는 필수입니다.")
        @Valid
        List<BrandDto> brands
) {
}
