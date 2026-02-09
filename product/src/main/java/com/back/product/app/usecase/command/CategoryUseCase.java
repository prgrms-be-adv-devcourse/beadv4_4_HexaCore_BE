package com.back.product.app.usecase.command;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.exception.InvalidValueException;
import com.back.product.adapter.out.persistence.CategoryRepository;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.Category;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.dto.request.CategoryListCreateRequestDto;
import com.back.product.mapper.CategoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<CategoryDto> createCategories(@Valid CategoryListCreateRequestDto request) {
        Map<String, Category> existsCategories = productSupport.getAllCategories().stream()
                .collect(Collectors.toMap(
                        category -> toPlainText(category.getName()),
                        category -> category
                ));

        List<Category> categoriesToCreate = request.categories().stream()
                .filter(newCategory -> {
                    String newName = toPlainText(newCategory.name());
                    return !existsCategories.containsKey(newName);
                })
                .map(categoryMapper::toEntity)
                .toList();

        List<Category> newCategories = categoryRepository.saveAll(categoriesToCreate);

        return newCategories.stream().map(categoryMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Category findCategoryExists(Long categoryId) {
        return productSupport.findCategoryById(categoryId)
                .orElseThrow(() -> new CustomException(FailureCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    public CategoryDto modifyCategory(Long categoryId, @Valid CategoryDataRequestDto request) {
        Category categoryToModify = findCategoryExists(categoryId);

        String newName = toPlainText(request.name());

        productSupport.getAllCategories().stream()
                .filter(existsCategory -> !existsCategory.getId().equals(categoryId))
                .map(existsCategory -> toPlainText(existsCategory.getName()))
                .filter(existsCategoryPlainName -> existsCategoryPlainName.equals(newName))
                .findFirst()
                .ifPresent(_ -> { throw new CustomException(FailureCode.CATEGORY_NAME_DUPLICATE); });

        categoryToModify.modifyName(request.name());

        categoryToModify.modifyImageUrl(request.imageUrl());

        return categoryMapper.toDto(categoryToModify);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    private String toPlainText(String text) {
        if (text == null) {
            throw new InvalidValueException();
        }

        return text.toLowerCase();
    }
}
