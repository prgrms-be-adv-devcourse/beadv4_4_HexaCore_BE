package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.*;
import com.back.product.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductSupport {
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductInfoRepository productInfoRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductRepository productRepository;
    private final ProductOptionValuesRepository productOptionValuesRepository;
    private final ProductImageRepository productImageRepository;

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

    @Transactional(readOnly = true)
    public Optional<Brand> findBrandById(Long brandId) {
        return brandRepository.findById(brandId);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Transactional(readOnly = true)
    public boolean existsProductInfoByBrandAndCode(Brand brand, String code) {
        return productInfoRepository.existsProductInfoByBrandAndProductCodeIgnoreCase(brand, code);
    }

    @Transactional(readOnly = true)
    public List<OptionValue> getAllOptionValues(List<Long> optionValueIds) {
        return optionValueRepository.findAllById(optionValueIds);
    }

    @Transactional(readOnly = true)
    public Optional<ProductInfo> findProductInfoById(Long productInfoId) {
        return productInfoRepository.findById(productInfoId);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsByProductInfo(ProductInfo productInfo) {
        return productRepository.findAllByProductInfo(productInfo);
    }

    @Transactional(readOnly = true)
    public List<ProductOptionValues> getAllProductOptionValuesByProductsIn(List<Product> products) {
        return productOptionValuesRepository.findAllByProductIn(products);
    }

    @Transactional(readOnly = true)
    public List<ProductImage> getAllProductImagesByProductsIn(List<Product> products) {
        return productImageRepository.findALlByProductIn(products);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsByProductInfoId(Long productInfoId) {
        ProductInfo productInfo = productInfoRepository.findById(productInfoId)
                .orElseThrow(() -> new CustomException(FailureCode.PRODUCT_INFO_NOT_FOUND));

        return productRepository.findAllByProductInfo(productInfo);
    }
}
