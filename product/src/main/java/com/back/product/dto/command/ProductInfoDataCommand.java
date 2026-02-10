package com.back.product.dto.command;

import com.back.product.domain.Brand;
import com.back.product.domain.Category;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductInfoDataCommand(
    Brand brand,
    Category category,
    String name,
    String code,
    BigDecimal releasePrice,
    LocalDateTime releasedDate
) {
}
