package com.back.product.mapper;

import com.back.product.domain.Brand;
import com.back.product.domain.Category;
import com.back.product.dto.command.ProductInfoDataCommand;
import com.back.product.dto.request.ProductInfoDataRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ProductInfoDataCommandMapper {
    public ProductInfoDataCommand toCommand(ProductInfoDataRequestDto request, Brand brand, Category category) {
        return ProductInfoDataCommand.builder()
                .brand(brand)
                .category(category)
                .name(request.name())
                .code(request.code())
                .releasePrice(request.releasePrice())
                .releasedDate(request.releasedDate())
                .build();
    }
}
