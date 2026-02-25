package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.event.ProductSpringEventPublisher;
import com.back.product.app.usecase.BrandUseCase;
import com.back.product.app.usecase.CategoryUseCase;
import com.back.product.app.usecase.ProductDocumentUseCase;
import com.back.product.app.usecase.ProductInfoUseCase;
import com.back.product.app.usecase.ProductUseCase;
import com.back.product.domain.Brand;
import com.back.product.domain.Category;
import com.back.product.domain.ProductInfo;
import com.back.product.dto.command.ProductInfoDataCommand;
import com.back.product.dto.command.ProductSearchCommand;
import com.back.product.dto.command.ProductVariantCreateCommand;
import com.back.product.dto.command.ProductVariantUpdateCommand;
import com.back.product.dto.model.ProductDetailDto;
import com.back.product.dto.model.ProductDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.dto.request.PageRequestDto;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductQueryRequestDto;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
import com.back.product.dto.response.ProductDetailListResponseDto;
import com.back.product.dto.response.ProductDetailResponseDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.mapper.ProductInfoDataCommandMapper;
import com.back.product.mapper.ProductInfoMapper;
import com.back.product.mapper.ProductMapper;
import com.back.product.mapper.ProductSearchCommandMapper;
import com.back.product.mapper.ProductVariantCreateCommandMapper;
import com.back.product.mapper.ProductVariantUpdateCommandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductSpringEventPublisher productSpringEventPublisher;

    private final BrandUseCase brandUseCase;
    private final CategoryUseCase categoryUseCase;
    private final ProductInfoUseCase productInfoUseCase;
    private final ProductUseCase productUseCase;
    private final ProductDocumentUseCase productDocumentUseCase;

    private final ProductInfoMapper productInfoMapper;
    private final ProductMapper productMapper;
    private final ProductInfoDataCommandMapper productInfoDataCommandMapper;
    private final ProductSearchCommandMapper productSearchCommandMapper;
    private final ProductVariantCreateCommandMapper productVariantCreateCommandMapper;
    private final ProductVariantUpdateCommandMapper productVariantUpdateCommandMapper;

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

        String thumbnailUrl = findThumbnailUrl(productDtos);
        productSpringEventPublisher.sendCreatedEvent(productInfoDto, productDtos, thumbnailUrl);

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

        String thumbnailUrl = findThumbnailUrl(productDtos);
        productSpringEventPublisher.sendModifiedEvent(productInfoDto, productDtos, thumbnailUrl);

        return productMapper.toResponseDto(productInfoDto, productDtos);
    }

    @Loggable
    @Transactional
    public void deleteProduct(Long productInfoId) {
        productUseCase.deleteMultipleProduct(productInfoId);

        productInfoUseCase.deleteProductInfo(productInfoId);

        productSpringEventPublisher.sendDeletedEvent(productInfoId);
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
    public ProductSearchResponseDto findProductPage(@Valid ProductSearchRequestDto request) {
        ProductSearchCommand productSearchCommand = productSearchCommandMapper.toCommand(request);
        return productDocumentUseCase.findProductPage(productSearchCommand);
    }

    @Loggable
    @Transactional(readOnly = true)
    public ProductSearchResponseDto findSimilarProducts(Long productInfoId, PageRequestDto request) {
        return productDocumentUseCase.findSimilarProducts(productInfoId, request.page(), request.size());
    }

    @Loggable
    @Transactional
    public void resyncProduct(Long productInfoId) {
        ProductInfo productInfo = productInfoUseCase.findProductInfo(productInfoId);
        List<ProductDto> productDtos = productUseCase.findAllProduct(productInfo);

        ProductInfoDto productInfoDto = productInfoMapper.toDto(productInfo);
        String thumbnail = findThumbnailUrl(productDtos);

        productSpringEventPublisher.sendModifiedEvent(productInfoDto, productDtos, thumbnail);
    }

    private String findThumbnailUrl(List<ProductDto> productDtos) {
        return productDtos.stream()
                .map(ProductDto::imageUrls)
                .flatMap(List::stream)
                .findFirst()
                .orElseThrow(() -> new CustomException(FailureCode.MISSING_REQUIRED_FIELD));
    }
}
