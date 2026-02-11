package com.back.product.dto.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailDto(
        @NotNull(message = "상품 정보는 필수입니다.")
        @Valid
        ProductInfoDto productInfo,

        @NotEmpty(message = "상품 목록은 비어 있을 수 없습니다.")
        @Valid
        List<ProductDto> products
) {
}
