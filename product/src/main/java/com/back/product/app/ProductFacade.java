package com.back.product.app;

import com.back.product.app.usecase.BrandUseCase;
import com.back.product.app.usecase.CategoryUseCase;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.BrandDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    private final BrandUseCase brandUseCase;
    private final CategoryUseCase categoryUseCase;

    @Transactional(readOnly = true)
    public List<BrandDto> getBrands() {
        return brandUseCase.getBrands();
    }

    @Transactional
    public BrandDto createBrand(@Valid BrandCreateRequestDto request) {
        return brandUseCase.createBrand(request);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return categoryUseCase.getCategories();
    }
}
