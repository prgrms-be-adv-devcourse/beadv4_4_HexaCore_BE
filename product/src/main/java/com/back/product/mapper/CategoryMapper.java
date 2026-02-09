package com.back.product.mapper;

import com.back.common.product.event.payload.CategoryPayload;
import com.back.product.domain.Category;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.request.CategoryCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .build();
    }

    public Category toEntity(CategoryCreateRequestDto.CategoryDto request) {
        return Category.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();
    }

    public CategoryDto toDto(CategoryPayload payload) {
        return CategoryDto.builder()
                .categoryId(payload.categoryId())
                .name(payload.name())
                .build();
    }
}
