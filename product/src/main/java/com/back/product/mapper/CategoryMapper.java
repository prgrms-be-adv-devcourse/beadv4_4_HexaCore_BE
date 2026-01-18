package com.back.product.mapper;

import com.back.product.domain.Category;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.request.CategoryCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .build();
    }

    public Category toEntity(CategoryCreateRequestDto request) {
        return Category.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();
    }
}
