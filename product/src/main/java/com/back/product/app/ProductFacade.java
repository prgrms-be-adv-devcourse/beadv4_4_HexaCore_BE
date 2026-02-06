package com.back.product.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.*;
import com.back.product.domain.*;
import com.back.product.dto.*;
import com.back.product.dto.request.*;
import com.back.product.dto.response.*;
import com.back.product.global.event.ProductCreationCompletedEvent;
import com.back.product.global.event.ProductDeletionCompletedEvent;
import com.back.product.global.event.ProductUpdateCompletedEvent;
import com.back.product.mapper.OptionMapper;
import com.back.product.mapper.ProductInfoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    private final OptionMapper optionMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

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
    public void deleteCategory(Long categoryId) {
        Boolean isUsed = productInfoUseCase.isCategoryInUse(categoryId);

        if (isUsed) {
            throw new CustomException(FailureCode.CATEGORY_IN_USE);
        }

        categoryUseCase.deleteCategory(categoryId);
    }

    @Transactional(readOnly = true)
    public ProductListResponseDto getProducts(@Valid ProductQueryRequestDto request) {
        return productUseCase.findMultipleProduct(request.productIds());
    }

    @Transactional
    public ProductResponseDto createProduct(@Valid ProductCreateRequestDto request) {
        Brand brand = brandUseCase.findBrandExists(request.productInfo().brandId());

        Category category = categoryUseCase.findCategoryExists(request.productInfo().categoryId());

        List<Long> optionValueIds = request.variants().stream()
                .flatMap(variant -> variant.optionValueIds().stream()).distinct().toList();
        List<OptionValue> optionValues = optionUseCase.findOptionValues(optionValueIds);

        ProductInfo productInfo = productInfoUseCase.createProductInfo(brand, category, request.productInfo());

        List<ProductDto> productDtos = productUseCase.createMultipleProduct(productInfo, request.variants(), optionValues);

        publishProductCreationCompletedEvent(
                productInfo,
                optionValues,
                productDtos.getFirst().imageUrls().getFirst()
        );

        return buildProductResponseDto(productInfo, productDtos);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long productInfoId, @Valid ProductUpdateRequestDto request) {
        Brand brand = brandUseCase.findBrandExists(request.productInfo().brandId());

        Category category = categoryUseCase.findCategoryExists(request.productInfo().categoryId());

        List<Long> optionValueIds = request.variants().stream()
                .flatMap(variant -> variant.optionValueIds().stream()).distinct().toList();
        List<OptionValue> optionValues = optionUseCase.findOptionValues(optionValueIds);

        ProductInfo productInfo = productInfoUseCase.updateProductInfo(productInfoId, brand, category, request.productInfo());

        List<ProductDto> productDtos = productUseCase.updateMultipleProduct(productInfo, request.variants(), optionValues);

        publishProductUpdateCompletedEvent(
                productInfo,
                optionValues,
                productDtos.getFirst().imageUrls().getFirst()
        );

        return buildProductResponseDto(productInfo, productDtos);
    }

    @Transactional
    public void deleteProduct(Long productInfoId) {
        productUseCase.deleteMultipleProduct(productInfoId);

        productInfoUseCase.deleteProductInfo(productInfoId);

        publishProductDeletionCompletedEvent(productInfoId);
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

    @Transactional
    public OptionListResponseDto createOptions(@Valid OptionCreateRequestDto request) {
        return optionUseCase.createOptions(request);
    }

    @Transactional
    public OptionResponseDto appendOptions(Long optionGroupId, @Valid OptionAppendRequestDto request) {
        return optionUseCase.appendOptions(optionGroupId, request);
    }

    @Transactional
    public OptionGroupModifyResponseDto modifyOptionGroup(Long optionGroupId, @Valid OptionGroupModifyRequestDto request) {
        return optionUseCase.modifyOptionGroup(optionGroupId, request);
    }

    @Transactional
    public OptionValueModifyResponseDto modifyOptionValue(Long optionValueId, @Valid OptionValueModifyRequestDto request) {
        return optionUseCase.modifyOptionValue(optionValueId, request);
    }

    @Transactional
    public void deleteOptionGroup(Long optionGroupId) {
        Boolean isUsed = productUseCase.isOptionGroupInUse(optionGroupId);

        if (isUsed) {
            throw new CustomException(FailureCode.OPTION_GROUP_IN_USE);
        }

        optionUseCase.deleteOptions(optionGroupId);
    }

    @Transactional
    public void deleteOptionValue(Long optionValueId) {
        Boolean isUsed = productUseCase.isOptionValueInUse(optionValueId);

        if (isUsed) {
            throw new CustomException(FailureCode.OPTION_VALUE_IN_USE);
        }

        optionUseCase.deleteOption(optionValueId);
    }

    @Transactional(readOnly = true)
    public OptionListResponseDto getOptions() {
        return optionUseCase.findAllOptions();
    }

    private ProductResponseDto buildProductResponseDto(ProductInfo productInfo, List<ProductDto> productDtos) {
        return ProductResponseDto.builder()
                .productInfo(productInfoMapper.toDto(productInfo))
                .products(productDtos)
                .build();
    }

    private void publishProductCreationCompletedEvent(ProductInfo productInfo, List<OptionValue> optionValues, String thumbnailUrl) {
        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        List<OptionDto> optionDtos = buildMultipleOptionDto(optionValues);

        ProductCreationCompletedEvent event = new ProductCreationCompletedEvent(
                productInfoDto, optionDtos, thumbnailUrl
        );

        applicationEventPublisher.publishEvent(event);
    }

    private void publishProductUpdateCompletedEvent(ProductInfo productInfo, List<OptionValue> optionValues, String thumbnailUrl) {
        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        List<OptionDto> optionDtos = buildMultipleOptionDto(optionValues);

        ProductUpdateCompletedEvent event = new ProductUpdateCompletedEvent(
                productInfoDto, optionDtos, thumbnailUrl
        );

        applicationEventPublisher.publishEvent(event);
    }

    private void publishProductDeletionCompletedEvent(Long productInfoId) {
        ProductDeletionCompletedEvent event = new ProductDeletionCompletedEvent(productInfoId);

        applicationEventPublisher.publishEvent(event);
    }

    private List<OptionDto> buildMultipleOptionDto(List<OptionValue> optionValues) {
        return optionValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getOptionGroup))
                .entrySet().stream().map(entry -> {
                    OptionGroup group = entry.getKey();
                    List<OptionValue> values = entry.getValue();
                    return optionMapper.toDto(group, values);
                }).toList();
    }
}
