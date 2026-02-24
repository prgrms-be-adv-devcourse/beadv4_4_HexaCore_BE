package com.back.notification.adapter.out.feign.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProductDetailDto {
    private ProductInfoDto productInfo;
    private List<ProductDto> products;

    public ProductDetailDto withFilteredProduct(Long productId) {
        ProductDetailDto filtered = new ProductDetailDto();
        filtered.productInfo = this.productInfo;
        filtered.products = this.products.stream()
                .filter(p -> p.getProductId().equals(productId))
                .toList();
        return filtered;
    }
}
