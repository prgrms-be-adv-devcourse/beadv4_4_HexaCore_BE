package com.back.product.dto.request;

import com.back.product.dto.enums.ProductSortType;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductSearchRequestDto(
        // 1. 검색어 (전문 검색)
        @Pattern(regexp = "^[A-Za-z0-9 ]{0,50}$", message = "Keyword must be alphanumeric and up to 50 characters")
        String keyword,

        // 2. 필터링 조건 (다중 선택 가능)
        @NotNull(message = "Brand Filter List cannot be null")
        @Size(max = 5, message = "Brand Filter List cannot be over 5")
        List<Long> brandIds,

        @NotNull(message = "Category Filter List cannot be null")
        @Size(max = 5, message = "Category Filter List cannot be over 5")
        List<Long> categoryIds,

        // 3. 범위 필터링 (가격)
        @DecimalMin(value = "0.00", message = "Minimum Price cannot be Negative Integer")
        BigDecimal minPrice,
        @DecimalMin(value = "0.00", message = "Maximum Price cannot be Negative Integer")
        BigDecimal maxPrice,

        // 4. 정렬 조건
        @NotNull(message = "Sort cannot be null")
        ProductSortType sort
) {
}
