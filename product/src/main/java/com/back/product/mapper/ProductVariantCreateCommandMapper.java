package com.back.product.mapper;

import com.back.product.dto.command.ProductVariantCreateCommand;
import com.back.product.dto.request.ProductVariantCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantCreateCommandMapper {
    public ProductVariantCreateCommand toCommand(ProductVariantCreateRequestDto request) {
        return ProductVariantCreateCommand.builder()
                .optionValueIds(request.optionValueIds())
                .inventory(request.inventory())
                .imageUrls(request.imageUrls())
                .build();
    }
}
