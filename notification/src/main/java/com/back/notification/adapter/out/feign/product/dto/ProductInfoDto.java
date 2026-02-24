package com.back.notification.adapter.out.feign.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ProductInfoDto {
    private Long productInfoId;
    private BrandDto brand;
    private CategoryDto category;
    private String name;
    private String code;
    private BigDecimal releasePrice;
    private LocalDateTime releaseDate;
}
