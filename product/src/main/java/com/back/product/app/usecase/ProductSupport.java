package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.BrandRepository;
import com.back.product.adapter.out.persistence.CategoryRepository;
import com.back.product.adapter.out.persistence.OptionGroupRepository;
import com.back.product.adapter.out.persistence.OptionValueRepository;
import com.back.product.adapter.out.persistence.ProductImageRepository;
import com.back.product.adapter.out.persistence.ProductInfoRepository;
import com.back.product.adapter.out.persistence.ProductOptionValuesRepository;
import com.back.product.adapter.out.persistence.ProductRepository;
import com.back.product.domain.Brand;
import com.back.product.domain.Category;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.domain.Product;
import com.back.product.domain.ProductImage;
import com.back.product.domain.ProductInfo;
import com.back.product.domain.ProductOptionValues;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final OptionGroupRepository optionGroupRepository;

    @Transactional(readOnly = true)
    public Page<Brand> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public boolean existsBrandByNameAndIdNot(String name, Long id) {
        return brandRepository.existsBrandByNameIgnoreCaseAndIdNot(name, id);
    }

    @Transactional(readOnly = true)
    public List<Brand> getAllBrandsByName(List<String> brandNames) {
        return brandRepository.findAllByNameIgnoreCaseIn(brandNames);
    }

    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesByName(List<String> newCategoryNames) {
        return categoryRepository.findAllByNameIgnoreCaseIn(newCategoryNames);
    }

    @Transactional(readOnly = true)
    public boolean existsCategoryByNameAndIdNot(String name, Long id) {
        return categoryRepository.existsCategoryByNameIgnoreCaseAndIdNot(name, id);
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

    @Transactional(readOnly = true)
    public Boolean existsProductInfoByBrand(Long brandId) {
        return productInfoRepository.existsByBrand_Id(brandId);
    }

    @Transactional(readOnly = true)
    public Boolean existsProductInfoByCategory(Long categoryId) {
        return productInfoRepository.existsByCategory_Id(categoryId);
    }

    @Transactional(readOnly = true)
    public List<OptionGroup> getAllProductOptionGroups() {
        return optionGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<OptionValue> getAllProductOptionValuesByOptionGroupIn(List<OptionGroup> productOptionGroups) {
        return optionValueRepository.findAllByOptionGroupIn(productOptionGroups);
    }

    @Transactional(readOnly = true)
    public OptionGroup getOptionGroupByName(String name) {
        return optionGroupRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<OptionGroup> getOptionGroupById(Long optionGroupId) {
        return optionGroupRepository.findById(optionGroupId);
    }

    @Transactional(readOnly = true)
    public Optional<OptionValue> getOptionValueById(Long optionValueId) {
        return optionValueRepository.findById(optionValueId);
    }

    @Transactional(readOnly = true)
    public Boolean existsProductByOptionGroupId(Long optionGroupId) {
        return productOptionValuesRepository.existsByOptionValue_OptionGroup_Id(optionGroupId);
    }

    @Transactional(readOnly = true)
    public Boolean existsProductByOptionValueId(Long optionValueId) {
        return productOptionValuesRepository.existsByOptionValue_Id(optionValueId);
    }

    @Transactional(readOnly = true)
    public List<Product> findMultipleProductByIds(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }
}
