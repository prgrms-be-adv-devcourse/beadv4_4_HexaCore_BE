package com.back.product.app.facade;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.command.*;
import com.back.product.domain.*;
import com.back.product.dto.model.*;
import com.back.product.dto.request.*;
import com.back.product.dto.response.*;
import com.back.product.dto.event.ProductCreationCompletedEvent;
import com.back.product.dto.event.ProductDeletionCompletedEvent;
import com.back.product.dto.event.ProductUpdateCompletedEvent;
import com.back.product.mapper.*;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    private final BrandUseCase brandUseCase;
    private final CategoryUseCase categoryUseCase;
    private final OptionUseCase optionUseCase;
    private final ProductInfoUseCase productInfoUseCase;
    private final ProductUseCase productUseCase;
    private final ProductDocumentUseCase productDocumentUseCase;

    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    private final ProductInfoMapper productInfoMapper;
    private final ProductMapper productMapper;
    private final OptionMapper optionMapper;

    @Transactional(readOnly = true)
    public List<BrandDto> getBrands() {
        return brandUseCase.getBrands();
    }

    @Transactional
    public BrandListResponseDto createBrands(@Valid BrandListCreateRequestDto request) {
        List<BrandDto> brandDtos = brandUseCase.createBrands(request);
        return brandMapper.toListResponseDto(brandDtos);
    }

    @Transactional
    public BrandResponseDto modifyBrand(Long brandId, @Valid BrandDataRequestDto request) {
        BrandDto brandDto = brandUseCase.modifyBrand(brandId, request);
        return brandMapper.toResponseDto(brandDto);
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
    public CategoryListResponseDto createCategories(@Valid CategoryListCreateRequestDto request) {
        List<CategoryDto> categoryDtos =  categoryUseCase.createCategories(request);
        return categoryMapper.toListResponseDto(categoryDtos);
    }

    @Transactional
    public CategoryResponseDto modifyCategory(Long categoryId, @Valid CategoryDataRequestDto request) {
        CategoryDto categoryDto = categoryUseCase.modifyCategory(categoryId, request);
        return categoryMapper.toResponseDto(categoryDto);
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

        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        return productMapper.toResponseDto(productInfoDto, productDtos);
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

        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        return productMapper.toResponseDto(productInfoDto, productDtos);
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

        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        return productMapper.toResponseDto(productInfoDto, productDtos);
    }

    @Transactional(readOnly = true)
    public ProductSearchResponseDto findProductPage(@Valid ProductSearchRequestDto request, Long page, Long size) {
        return productDocumentUseCase.findProductPage(request, page, size);
    }

    @Transactional
    public OptionListResponseDto createOptions(@Valid OptionListCreateRequestDto request) {
        List<OptionDto> optionDtos = optionUseCase.createOptions(request);
        return optionMapper.toListResponseDto(optionDtos);
    }

    @Transactional
    public OptionResponseDto appendOptions(Long optionGroupId, @Valid OptionAppendRequestDto request) {
        OptionDto optionDto = optionUseCase.appendOptions(optionGroupId, request);
        return optionMapper.toResponseDto(optionDto);
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
        List<OptionDto> optionDtos = optionUseCase.findAllOptions();
        return optionMapper.toListResponseDto(optionDtos);
    }

    private void publishProductCreationCompletedEvent(ProductInfo productInfo, List<OptionValue> optionValues, String thumbnailUrl) {
        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        List<OptionDto> optionDtos = optionMapper.toDtoList(optionValues);

        ProductCreationCompletedEvent event = new ProductCreationCompletedEvent(
                productInfoDto, optionDtos, thumbnailUrl
        );

        applicationEventPublisher.publishEvent(event);
    }

    private void publishProductUpdateCompletedEvent(ProductInfo productInfo, List<OptionValue> optionValues, String thumbnailUrl) {
        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);
        List<OptionDto> optionDtos = optionMapper.toDtoList(optionValues);

        ProductUpdateCompletedEvent event = new ProductUpdateCompletedEvent(
                productInfoDto, optionDtos, thumbnailUrl
        );

        applicationEventPublisher.publishEvent(event);
    }

    private void publishProductDeletionCompletedEvent(Long productInfoId) {
        ProductDeletionCompletedEvent event = new ProductDeletionCompletedEvent(productInfoId);

        applicationEventPublisher.publishEvent(event);
    }
}
