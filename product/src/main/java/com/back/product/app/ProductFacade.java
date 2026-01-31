package com.back.product.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.*;
import com.back.product.domain.*;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.ProductDto;
import com.back.product.dto.request.*;
import com.back.product.dto.BrandDto;
import com.back.product.dto.response.*;
import com.back.product.mapper.ProductInfoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    private final BrandUseCase brandUseCase;
    private final CategoryUseCase categoryUseCase;
    private final OptionUseCase optionUseCase;
    private final ProductInfoUseCase productInfoUseCase;
    private final ProductUseCase productUseCase;
    private final ProductInfoMapper productInfoMapper;
    private final ProductDocumentUseCase productDocumentUseCase;

    @Transactional(readOnly = true)
    public List<BrandDto> getBrands() {
        return brandUseCase.getBrands();
    }

    @Transactional
    public BrandListResponseDto createBrands(@Valid BrandCreateRequestDto request) {
        List<BrandDto> brandDtos = brandUseCase.createBrands(request);
        return BrandListResponseDto.builder().brands(brandDtos).build();
    }

    @Transactional
    public BrandResponseDto modifyBrand(Long brandId, @Valid BrandModifyRequestDto request) {
        BrandDto brandDto = brandUseCase.modifyBrand(brandId, request);
        return BrandResponseDto.builder().brand(brandDto).build();
    }

    @Transactional
    public void deleteBrand(Long brandId) {
        Boolean isUsed = productInfoUseCase.isBrandInUse(brandId);

        if (isUsed) {
            throw new CustomException(FailureCode.BRAND_IN_USE);
        }

        brandUseCase.deleteBrand(brandId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return categoryUseCase.getCategories();
    }

    @Transactional
    public CategoryListResponseDto createCategories(@Valid CategoryCreateRequestDto request) {
        List<CategoryDto> categoryDtos =  categoryUseCase.createCategories(request);
        return CategoryListResponseDto.builder().categories(categoryDtos).build();
    }

    @Transactional
    public CategoryResponseDto modifyCategory(Long categoryId, @Valid CategoryModifyRequestDto request) {
        CategoryDto categoryDto = categoryUseCase.modifyCategory(categoryId, request);
        return CategoryResponseDto.builder().category(categoryDto).build();
    }

    @Transactional
    public ProductResponseDto createProduct(@Valid ProductCreateRequestDto request) {
        Brand brand = brandUseCase.findBrandExists(request.productInfo().brandId());

        Category category = categoryUseCase.findCategoryExists(request.productInfo().categoryId());

        List<Long> optionValueIds = request.variants().stream()
                .flatMap(variant -> variant.optionValueIds().stream()).distinct().toList();
        Map<Long, OptionValue> optionValueMap = optionUseCase.findOptionValuesAsMap(optionValueIds);

        ProductInfo productInfo = productInfoUseCase.createProductInfo(brand, category, request.productInfo());

        List<ProductDto> productDtos = productUseCase.createMultipleProduct(productInfo, request.variants(), optionValueMap);

        return buildProductResponseDto(productInfo, productDtos);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long productInfoId, @Valid ProductUpdateRequestDto request) {
        Brand brand = brandUseCase.findBrandExists(request.productInfo().brandId());

        Category category = categoryUseCase.findCategoryExists(request.productInfo().categoryId());

        List<Long> optionValueIds = request.variants().stream()
                .flatMap(variant -> variant.optionValueIds().stream()).distinct().toList();
        Map<Long, OptionValue> optionValueMap = optionUseCase.findOptionValuesAsMap(optionValueIds);

        ProductInfo productInfo = productInfoUseCase.updateProductInfo(productInfoId, brand, category, request.productInfo());

        List<ProductDto> productDtos = productUseCase.updateMultipleProduct(productInfo, request.variants(), optionValueMap);

        return buildProductResponseDto(productInfo, productDtos);
    }

    @Transactional
    public void deleteProduct(Long productInfoId) {
        productUseCase.deleteMultipleProduct(productInfoId);

        productInfoUseCase.deleteProductInfo(productInfoId);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductDetail(Long productInfoId) {
        ProductInfo productInfo = productInfoUseCase.findProductInfo(productInfoId);

        List<ProductDto> productDtos = productUseCase.findAllProduct(productInfo);

        return buildProductResponseDto(productInfo, productDtos);
    }

    @Transactional(readOnly = true)
    public ProductSearchListResponseDto findProductPage(@Valid ProductSearchRequestDto request, Long page, Long size) {
        return productDocumentUseCase.findProductPage(request, page, size);
    }

    private ProductResponseDto buildProductResponseDto(ProductInfo productInfo, List<ProductDto> productDtos) {
        return ProductResponseDto.builder()
                .productInfo(productInfoMapper.toDto(productInfo))
                .products(productDtos)
                .build();
    }
}
