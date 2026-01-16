package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.CategoryRepository;
import com.back.product.domain.Category;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.request.CategoryCreateRequestDto;
import com.back.product.mapper.CategoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryUseCase {
    private final CategoryMapper categoryMapper;
    private final ProductSupport productSupport;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return productSupport.getAllCategories().stream().map(categoryMapper::toDto).toList();
    }

    @Transactional
    public CategoryDto createCategory(@Valid CategoryCreateRequestDto request) {
        isDuplicateCategoryName(request.name());

        Category category = categoryMapper.toEntity(request);

        Category newCategory = categoryRepository.save(category);

        return categoryMapper.toDto(newCategory);
    }

    private void isDuplicateCategoryName(String name) {
        if (productSupport.existsCategoryByName(name)) {
            throw new CustomException(FailureCode.CATEGORY_NAME_DUPLICATE);
        }
    }
}
