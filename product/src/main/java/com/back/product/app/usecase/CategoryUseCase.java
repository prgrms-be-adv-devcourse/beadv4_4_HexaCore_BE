package com.back.product.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.exception.InvalidValueException;
import com.back.product.adapter.out.persistence.CategoryRepository;
import com.back.product.domain.Category;
import com.back.product.dto.command.CategoryDataCommand;
import com.back.product.dto.model.CategoryDto;
import com.back.product.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Loggable
    @Transactional(readOnly = true)
    public Page<CategoryDto> getCategories(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return productSupport.getAllCategories(pageable).map(categoryMapper::toDto);
    }

    @Loggable
    @Transactional
    public List<CategoryDto> createCategories(List<CategoryDataCommand> categories) {
        List<String> newCategoryNames = categories.stream().map(category -> category.name().toLowerCase()).toList();

        Map<String, Category> existsCategories = productSupport.getAllCategoriesByName(newCategoryNames).stream()
                .collect(Collectors.toMap(
                        category -> toPlainText(category.getName()),
                        category -> category
                ));

        List<Category> categoriesToCreate = categories.stream()
                .filter(newCategory -> {
                    String newName = toPlainText(newCategory.name());
                    return !existsCategories.containsKey(newName);
                })
                .map(categoryMapper::toEntity)
                .toList();

        List<Category> newCategories = categoryRepository.saveAll(categoriesToCreate);

        return newCategories.stream().map(categoryMapper::toDto).toList();
    }

    @Loggable
    @Transactional(readOnly = true)
    public Category findCategoryExists(Long categoryId) {
        return productSupport.findCategoryById(categoryId)
                .orElseThrow(() -> new CustomException(FailureCode.CATEGORY_NOT_FOUND));
    }

    @Loggable
    @Transactional
    public CategoryDto modifyCategory(Long categoryId, CategoryDataCommand category) {
        Category categoryToModify = findCategoryExists(categoryId);

        String newName = toPlainText(category.name());

        if (productSupport.existsCategoryByNameAndIdNot(newName, categoryToModify.getId())) {
            throw new CustomException(FailureCode.CATEGORY_NAME_DUPLICATE);
        }

        categoryToModify.modifyName(category.name());

        categoryToModify.modifyImageUrl(category.imageUrl());

        return categoryMapper.toDto(categoryToModify);
    }

    @Loggable
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
