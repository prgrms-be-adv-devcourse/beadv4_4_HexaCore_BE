package com.back.product.dto.command;

import com.back.product.dto.enums.ProductSortType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductSearchCommand(
    String keyword,
    List<Long> brandIds,
    List<Long> categoryIds,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    ProductSortType sort,
    Long page,
    Long size
) {
}
