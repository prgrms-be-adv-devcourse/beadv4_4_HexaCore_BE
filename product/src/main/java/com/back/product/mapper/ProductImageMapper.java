package com.back.product.mapper;

import com.back.product.domain.Product;
import com.back.product.domain.ProductImage;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {
    public ProductImage toEntity(Product product, String url) {
        return ProductImage.builder()
                .product(product)
                .imageUrl(url)
                .build();
    }

    public String toDto(ProductImage productImage) {
        return productImage.getImageUrl();
    }
}
