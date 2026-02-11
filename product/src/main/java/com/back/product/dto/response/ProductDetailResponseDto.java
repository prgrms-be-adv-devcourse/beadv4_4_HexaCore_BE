package com.back.product.dto.response;

import com.back.product.dto.model.ProductDetailDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProductDetailResponseDto(
        @NotNull(message = "상품 상세 정보는 필수입니다.")
        @Valid
        ProductDetailDto product
) {
}
