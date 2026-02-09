package com.back.product.mapper;

import com.back.product.dto.command.CategoryDataCommand;
import com.back.product.dto.request.CategoryDataRequestDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryDataCommandMapper {
    public CategoryDataCommand toCommand(CategoryDataRequestDto request) {
        return CategoryDataCommand.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();
    }
}
