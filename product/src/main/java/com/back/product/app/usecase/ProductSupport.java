package com.back.product.app.usecase;

import com.back.product.adapter.out.BrandRepository;
import com.back.product.adapter.out.CategoryRepository;
import com.back.product.domain.Brand;
import com.back.product.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSupport {
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsBrandByName(String name) {
        return brandRepository.existsBrandByNameIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsCategoryByName(String name) {
        return categoryRepository.existsCategoryByNameIgnoreCase(name);
    }
}
