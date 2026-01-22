package com.back.product.mapper;

import com.back.product.domain.ProductOptionValues;
import com.back.product.dto.ProductOptionDto;
import org.springframework.stereotype.Component;

@Component
public class ProductOptionMapper {
    public ProductOptionDto toDto(ProductOptionValues productOptionValues) {
        return ProductOptionDto.builder()
                .productOptionValueId(productOptionValues.getId())
                .groupName(productOptionValues.getOptionValue().getOptionGroup().getName())
                .value(productOptionValues.getOptionValue().getValue())
                .build();
    }
}
