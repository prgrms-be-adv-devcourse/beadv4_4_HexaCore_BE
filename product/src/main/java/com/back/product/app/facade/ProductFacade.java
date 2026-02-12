package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.command.*;
import com.back.product.domain.*;
import com.back.product.dto.command.*;
import com.back.product.dto.model.*;
import com.back.product.dto.request.*;
import com.back.product.dto.response.*;
import com.back.product.dto.event.spring.ProductCreationCompletedEvent;
import com.back.product.dto.event.spring.ProductDeletionCompletedEvent;
import com.back.product.dto.event.spring.ProductUpdateCompletedEvent;
import com.back.product.mapper.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final SpringEventMapper springEventMapper;

    private final BrandDataCommandMapper brandDataCommandMapper;
    private final CategoryDataCommandMapper categoryDataCommandMapper;
    private final OptionCreateCommandMapper optionCreateCommandMapper;
    private final ProductInfoDataCommandMapper productInfoDataCommandMapper;
    private final ProductSearchCommandMapper productSearchCommandMapper;
    private final ProductVariantCreateCommandMapper productVariantCreateCommandMapper;
    private final ProductVariantUpdateCommandMapper productVariantUpdateCommandMapper;

    @Loggable
    @Transactional(readOnly = true)
    public List<BrandDto> getBrands() {
        return brandUseCase.getBrands();
    }

    @Loggable
    @Transactional
    public BrandListResponseDto createBrands(@Valid BrandListCreateRequestDto request) {
        List<BrandDataCommand> brandsCommands = request.brands().stream().map(brandDataCommandMapper::toCommand).toList();
        List<BrandDto> brandDtos = brandUseCase.createBrands(brandsCommands);
        return brandMapper.toListResponseDto(brandDtos);
    }

    @Loggable
    @Transactional
    public BrandResponseDto modifyBrand(Long brandId, @Valid BrandDataRequestDto request) {
        BrandDataCommand brandCommand = brandDataCommandMapper.toCommand(request);
        BrandDto brandDto = brandUseCase.modifyBrand(brandId, brandCommand);
        return brandMapper.toResponseDto(brandDto);
    }

    @Loggable
    @Transactional
    public void deleteBrand(Long brandId) {
        Boolean isUsed = productInfoUseCase.isBrandInUse(brandId);

        if (isUsed) {
            throw new CustomException(FailureCode.BRAND_IN_USE);
        }

        brandUseCase.deleteBrand(brandId);
    }

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

    @Loggable
    @Transactional(readOnly = true)
    public ProductDetailListResponseDto getProducts(@Valid ProductQueryRequestDto request) {
        List<ProductDetailDto> productDetailDtos = productUseCase.findMultipleProduct(request.productIds());
        return productMapper.toListResponseDto(productDetailDtos);
    }

    @Loggable
    @Transactional
    public ProductDetailResponseDto createProduct(@Valid ProductCreateRequestDto request) {
        Brand brand = brandUseCase.findBrandExists(request.productInfo().brandId());

        Category category = categoryUseCase.findCategoryExists(request.productInfo().categoryId());

        ProductInfoDataCommand productInfoCommand = productInfoDataCommandMapper.toCommand(request.productInfo(), brand, category);
        ProductInfo productInfo = productInfoUseCase.createProductInfo(productInfoCommand);
        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        List<ProductVariantCreateCommand> productVariantCreateCommands = request.variants().stream()
                .map(productVariantCreateCommandMapper::toCommand).toList();
        List<ProductDto> productDtos = productUseCase.createMultipleProduct(productInfo, productVariantCreateCommands);

        publishProductCreateEvent(productInfoDto, productDtos);

        return productMapper.toResponseDto(productInfoDto, productDtos);
    }

    @Loggable
    @Transactional
    public ProductDetailResponseDto updateProduct(Long productInfoId, @Valid ProductUpdateRequestDto request) {
        Brand brand = brandUseCase.findBrandExists(request.productInfo().brandId());

        Category category = categoryUseCase.findCategoryExists(request.productInfo().categoryId());

        ProductInfoDataCommand productInfoCommand = productInfoDataCommandMapper.toCommand(request.productInfo(), brand, category);
        ProductInfo productInfo = productInfoUseCase.updateProductInfo(productInfoId, productInfoCommand);
        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        List<ProductVariantUpdateCommand> productVariantUpdateCommands = request.variants().stream()
                .map(productVariantUpdateCommandMapper::toCommand).toList();
        List<ProductDto> productDtos = productUseCase.updateMultipleProduct(productInfo, productVariantUpdateCommands);

        publishProductUpdateEvent(productInfoDto, productDtos);

        return productMapper.toResponseDto(productInfoDto, productDtos);
    }

    @Loggable
    @Transactional
    public void deleteProduct(Long productInfoId) {
        productUseCase.deleteMultipleProduct(productInfoId);

        productInfoUseCase.deleteProductInfo(productInfoId);

        publishProductDeleteEvent(productInfoId);
    }

    @Loggable
    @Transactional(readOnly = true)
    public ProductDetailResponseDto getProductDetail(Long productInfoId) {
        ProductInfo productInfo = productInfoUseCase.findProductInfo(productInfoId);

        List<ProductDto> productDtos = productUseCase.findAllProduct(productInfo);

        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);

        return productMapper.toResponseDto(productInfoDto, productDtos);
    }

    @Loggable
    @Transactional(readOnly = true)
    public ProductSearchResponseDto findProductPage(@Valid ProductSearchRequestDto request, Long page, Long size) {
        ProductSearchCommand productSearchCommand = productSearchCommandMapper.toCommand(request, page, size);
        return productDocumentUseCase.findProductPage(productSearchCommand);
    }

    @Loggable
    @Transactional(readOnly = true)
    public ProductSearchResponseDto findSimilarProducts(Long productInfoId, Long count) {
        return productDocumentUseCase.findSimilarProducts(productInfoId, count);
    }

    @Loggable
    @Transactional
    public OptionListResponseDto createOptions(@Valid OptionListCreateRequestDto request) {
        List<OptionCreateCommand> optionCreateCommands = request.options().stream().map(optionCreateCommandMapper::toCommand).toList();
        List<OptionDto> optionDtos = optionUseCase.createOptions(optionCreateCommands);
        return optionMapper.toListResponseDto(optionDtos);
    }

    @Loggable
    @Transactional
    public OptionResponseDto appendOptions(Long optionGroupId, @Valid OptionAppendRequestDto request) {
        OptionDto optionDto = optionUseCase.appendOptions(optionGroupId, request.values());
        return optionMapper.toResponseDto(optionDto);
    }

    @Loggable
    @Transactional
    public OptionGroupModifyResponseDto modifyOptionGroup(Long optionGroupId, @Valid OptionGroupModifyRequestDto request) {
        return optionUseCase.modifyOptionGroup(optionGroupId, request.name());
    }

    @Loggable
    @Transactional
    public OptionValueModifyResponseDto modifyOptionValue(Long optionValueId, @Valid OptionValueModifyRequestDto request) {
        return optionUseCase.modifyOptionValue(request.optionGroupId(), optionValueId, request.name());
    }

    @Loggable
    @Transactional
    public void deleteOptionGroup(Long optionGroupId) {
        Boolean isUsed = productUseCase.isOptionGroupInUse(optionGroupId);

        if (isUsed) {
            throw new CustomException(FailureCode.OPTION_GROUP_IN_USE);
        }

        optionUseCase.deleteOptions(optionGroupId);
    }

    @Loggable
    @Transactional
    public void deleteOptionValue(Long optionValueId) {
        Boolean isUsed = productUseCase.isOptionValueInUse(optionValueId);

        if (isUsed) {
            throw new CustomException(FailureCode.OPTION_VALUE_IN_USE);
        }

        optionUseCase.deleteOption(optionValueId);
    }

    @Loggable
    @Transactional(readOnly = true)
    public OptionListResponseDto getOptions() {
        List<OptionDto> optionDtos = optionUseCase.findAllOptions();
        return optionMapper.toListResponseDto(optionDtos);
    }

    private void publishProductUpdateEvent(ProductInfoDto productInfoDto, List<ProductDto> productDtos) {
        List<OptionDto> optionDtos = productDtos.stream()
                .flatMap(productDto -> productDto.options().stream())
                .toList();

        String thumbnailUrl = findThumbnailUrl(productDtos);

        ProductUpdateCompletedEvent event = springEventMapper.toProductUpdatedEvent(productInfoDto, optionDtos, thumbnailUrl);

        applicationEventPublisher.publishEvent(event);
    }

    private void publishProductCreateEvent(ProductInfoDto productInfoDto, List<ProductDto> productDtos) {
        List<OptionDto> optionDtos = productDtos.stream()
                .flatMap(productDto -> productDto.options().stream())
                .toList();

        String thumbnailUrl = findThumbnailUrl(productDtos);

        ProductCreationCompletedEvent event = springEventMapper.toProductCreatedEvent(productInfoDto, optionDtos, thumbnailUrl);

        applicationEventPublisher.publishEvent(event);
    }

    private void publishProductDeleteEvent(Long productInfoId) {
        ProductDeletionCompletedEvent event = springEventMapper.toProductDeletedEvent(productInfoId);

        applicationEventPublisher.publishEvent(event);
    }

    private String findThumbnailUrl(List<ProductDto> productDtos) {
        return productDtos.stream()
                .map(ProductDto::imageUrls)
                .flatMap(List::stream)
                .findFirst()
                .orElseThrow(() -> new CustomException(FailureCode.MISSING_REQUIRED_FIELD));
    }
}
