package com.back.product.dto.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Builder
public record ProductDto(
        @NotNull(message = "상품 ID는 필수입니다.")
        @Min(value = 1, message = "상품 ID는 1 이상의 정수여야 합니다.")
        Long productId,

        @NotNull(message = "재고는 필수입니다.")
        @Min(value = 0, message = "재고는 0 이상의 값이어야 합니다.")
        Long inventory,

        @NotEmpty(message = "옵션 목록은 비어 있을 수 없습니다.")
        @Valid
        List<OptionDto> options,

        @NotEmpty(message = "이미지 URL 목록은 비어 있을 수 없습니다.")
        List<@URL(message = "유효한 URL 형식이 아닙니다.") String> imageUrls
) {
}

