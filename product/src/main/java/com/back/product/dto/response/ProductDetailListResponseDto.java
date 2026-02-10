package com.back.product.dto.response;

import com.back.product.dto.model.ProductDetailDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailListResponseDto(
        @NotEmpty(message = "상품 상세 목록은 비어 있을 수 없습니다.")
        @Valid
        List<ProductDetailDto> products
) {
}
