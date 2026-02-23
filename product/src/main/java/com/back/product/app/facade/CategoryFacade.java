package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.CategoryUseCase;
import com.back.product.app.usecase.ProductInfoUseCase;
import com.back.product.dto.command.CategoryDataCommand;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.dto.request.CategoryListCreateRequestDto;
import com.back.product.dto.response.CategoryListResponseDto;
import com.back.product.dto.response.CategoryResponseDto;
import com.back.product.mapper.CategoryDataCommandMapper;
import com.back.product.mapper.CategoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryFacade {
    private final CategoryUseCase categoryUseCase;
    private final CategoryMapper categoryMapper;
    private final ProductInfoUseCase productInfoUseCase;
    private final CategoryDataCommandMapper categoryDataCommandMapper;

    @Loggable
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return categoryUseCase.getCategories();
    }

    @Loggable
    @Transactional
    public CategoryListResponseDto createCategories(@Valid CategoryListCreateRequestDto request) {
        List<CategoryDataCommand> categoryCommands = request.categories().stream().map(categoryDataCommandMapper::toCommand).toList();
        List<CategoryDto> categoryDtos =  categoryUseCase.createCategories(categoryCommands);
        return categoryMapper.toListResponseDto(categoryDtos);
    }

    @Loggable
    @Transactional
    public CategoryResponseDto modifyCategory(Long categoryId, @Valid CategoryDataRequestDto request) {
        CategoryDataCommand categoryCommand = categoryDataCommandMapper.toCommand(request);
        CategoryDto categoryDto = categoryUseCase.modifyCategory(categoryId, categoryCommand);
        return categoryMapper.toResponseDto(categoryDto);
    }

    @Loggable
    @Transactional
    public void deleteCategory(Long categoryId) {
        Boolean isUsed = productInfoUseCase.isCategoryInUse(categoryId);

        if (isUsed) {
            throw new CustomException(FailureCode.CATEGORY_IN_USE);
        }

        categoryUseCase.deleteCategory(categoryId);
    }
}
