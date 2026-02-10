package com.back.product.mapper;

import com.back.product.dto.command.ProductSearchCommand;
import com.back.product.dto.request.ProductSearchRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchCommandMapper {
    public ProductSearchCommand toCommand(ProductSearchRequestDto request, Long page, Long size) {
        return ProductSearchCommand.builder()
                .keyword(request.keyword())
                .categoryIds(request.categoryIds())
                .brandIds(request.brandIds())
                .minPrice(request.minPrice())
                .maxPrice(request.maxPrice())
                .sort(request.sort())
                .page(page)
                .size(size)
                .build();
    }
}
