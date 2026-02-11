package com.back.product.mapper;

import com.back.product.dto.command.ProductVariantUpdateCommand;
import com.back.product.dto.request.ProductVariantUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantUpdateCommandMapper {
    public ProductVariantUpdateCommand toCommand(ProductVariantUpdateRequestDto request) {
        return ProductVariantUpdateCommand.builder()
                .productId(request.productId())
                .optionValueIds(request.optionValueIds())
                .inventory(request.inventory())
                .imageUrls(request.imageUrls())
                .build();
    }
}
