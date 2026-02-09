package com.back.product.mapper;

import com.back.common.product.event.payload.CategoryPayload;
import com.back.product.document.ProductDocument;
import com.back.product.domain.Category;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.dto.response.CategoryListResponseDto;
import com.back.product.dto.response.CategoryResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .build();
    }

    public CategoryDto toDto(CategoryPayload payload) {
        return CategoryDto.builder()
                .categoryId(payload.categoryId())
                .name(payload.name())
                .build();
    }

    public Category toEntity(CategoryDataRequestDto request) {
        return Category.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();
    }

    public ProductDocument.Category toDocument(CategoryDto dto) {
        return ProductDocument.Category.builder()
                .categoryId(dto.categoryId())
                .categoryName(dto.name())
                .build();
    }

    public CategoryResponseDto toResponseDto(CategoryDto categoryDto) {
        return CategoryResponseDto.builder()
                .category(categoryDto)
                .build();
    }

    public CategoryListResponseDto toListResponseDto(List<CategoryDto> categoryDtos) {
        return CategoryListResponseDto.builder()
                .categories(categoryDtos)
                .build();
    }

    public CategoryPayload toPayload(CategoryDto categoryDto) {
        return new CategoryPayload(
                categoryDto.categoryId(),
                categoryDto.name()
        );
    }
}
