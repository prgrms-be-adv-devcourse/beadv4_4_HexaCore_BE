package com.back.notification.adapter.out.feign.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProductDetailListResponse {
    private List<ProductDetailDto> products;
}
