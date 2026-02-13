package com.back.product.mapper;

import com.back.product.dto.command.ProductSearchCommand;
import com.back.product.dto.request.ProductSearchRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchCommandMapper {
    public ProductSearchCommand toCommand(ProductSearchRequestDto request) {
        return ProductSearchCommand.builder()
                .keyword(request.keyword())
                .categoryIds(request.categoryIds())
                .brandIds(request.brandIds())
                .minPrice(request.minPrice())
                .maxPrice(request.maxPrice())
                .sort(request.sort())
                .page(request.pageRequest().page())
                .size(request.pageRequest().size())
                .build();
    }
}
