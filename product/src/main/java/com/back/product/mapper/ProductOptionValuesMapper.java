package com.back.product.mapper;

import com.back.product.domain.OptionValue;
import com.back.product.domain.Product;
import com.back.product.domain.ProductOptionValues;
import org.springframework.stereotype.Component;

@Component
public class ProductOptionValuesMapper {
    public ProductOptionValues toEntity(Product product, OptionValue optionValue) {
        return ProductOptionValues.builder()
                .product(product)
                .optionValue(optionValue)
                .build();
    }
}
