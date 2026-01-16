package com.back.product.mapper;

import com.back.product.domain.Category;
import com.back.product.dto.CategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .build();
    }

    public Category toEntity(CategoryDto categoryDto) {
        return Category.builder()
                .name(categoryDto.name())
                .imageUrl(categoryDto.imageUrl())
                .build();
    }
}
