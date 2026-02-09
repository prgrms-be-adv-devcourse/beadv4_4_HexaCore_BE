package com.back.product.mapper;

import com.back.product.domain.ProductOptionValues;
import com.back.product.dto.model.ProductOptionValueDto;
import org.springframework.stereotype.Component;

@Component
public class ProductOptionMapper {
    public ProductOptionValueDto toDto(ProductOptionValues productOptionValues) {
        return ProductOptionValueDto.builder()
                .productOptionValueId(productOptionValues.getId())
                .groupName(productOptionValues.getOptionValue().getOptionGroup().getName())
                .value(productOptionValues.getOptionValue().getValue())
                .build();
    }

}
