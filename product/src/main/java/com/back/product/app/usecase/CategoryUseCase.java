package com.back.product.app.usecase;

import com.back.product.dto.CategoryDto;
import com.back.product.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryUseCase {
    private final CategoryMapper categoryMapper;
    private final ProductSupport productSupport;

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return productSupport.getAllCategories().stream().map(categoryMapper::toDto).toList();
    }
}
