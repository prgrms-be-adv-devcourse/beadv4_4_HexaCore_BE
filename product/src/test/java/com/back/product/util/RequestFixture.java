package com.back.product.util;

import com.back.product.dto.BrandDto;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.ProductDto;
import com.back.product.dto.ProductInfoDto;
import com.back.product.dto.ProductOptionDto;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductInfoCreateRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
import com.back.product.dto.request.ProductInfoUpdateRequestDto;
import com.back.product.dto.request.ProductVariantCreateRequestDto;
import com.back.product.dto.request.ProductVariantUpdateRequestDto;
import com.back.product.dto.response.ProductResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RequestFixture {

    public static ProductCreateRequestDto createProductCreateRequest() {
        ProductInfoCreateRequestDto productInfo = ProductInfoCreateRequestDto.builder()
                .brandId(1L)
                .categoryId(1L)
                .name("Test Product")
                .code("TP001")
                .releasePrice(BigDecimal.valueOf(10000))
                .releasedDate(LocalDateTime.of(2026, 1, 21, 0, 0))
                .build();

        ProductVariantCreateRequestDto variant = ProductVariantCreateRequestDto.builder()
                .optionValueIds(List.of(1L, 2L))
                .inventory(100L)
                .imageUrls(List.of("http://example.com/image.jpg"))
                .build();

        return ProductCreateRequestDto.builder()
                .productInfo(productInfo)
                .variants(List.of(variant))
                .build();
    }

    public static ProductUpdateRequestDto createProductUpdateRequest() {
        ProductInfoUpdateRequestDto productInfo = ProductInfoUpdateRequestDto.builder()
                .brandId(1L)
                .categoryId(1L)
                .name("Updated Product")
                .code("TP001-U")
                .releasePrice(BigDecimal.valueOf(12000))
                .releasedDate(LocalDateTime.of(2026, 1, 21, 0, 0))
                .build();

        ProductVariantUpdateRequestDto variant = ProductVariantUpdateRequestDto.builder()
                .productId(1L)
                .optionValueIds(List.of(1L, 2L))
                .inventory(150L)
                .imageUrls(List.of("http://example.com/updated_image.jpg"))
                .build();

        return ProductUpdateRequestDto.builder()
                .productInfo(productInfo)
                .variants(List.of(variant))
                .build();
    }

    public static ProductResponseDto createProductResponse() {
        BrandDto brand = BrandDto.builder()
                .brandId(1L)
                .name("Test Brand")
                .logoUrl("http://example.com/logo.png")
                .build();
        CategoryDto category = CategoryDto.builder()
                .categoryId(1L)
                .name("Test Category")
                .imageUrl("http://example.com/category.png")
                .build();

        ProductInfoDto productInfo = ProductInfoDto.builder()
                .productInfoId(1L)
                .brand(brand)
                .category(category)
                .name("Test Product")
                .code("TP001")
                .releasePrice(BigDecimal.valueOf(10000))
                .releaseDate(LocalDateTime.now())
                .build();

        ProductOptionDto option = ProductOptionDto.builder()
                .productOptionValueId(1L)
                .groupName("Color")
                .value("Black")
                .build();
        ProductDto product = ProductDto.builder()
                .productId(1L)
                .inventory(100L)
                .options(List.of(option))
                .imageUrls(List.of("http://example.com/image.jpg"))
                .build();

        return ProductResponseDto.builder()
                .productInfo(productInfo)
                .products(List.of(product))
                .build();
    }

    public static ProductResponseDto createUpdatedProductResponse() {
        BrandDto brand = BrandDto.builder()
                .brandId(1L)
                .name("Test Brand")
                .logoUrl("http://example.com/logo.png")
                .build();
        CategoryDto category = CategoryDto.builder()
                .categoryId(1L)
                .name("Test Category")
                .imageUrl("http://example.com/category.png")
                .build();

        ProductInfoDto productInfo = ProductInfoDto.builder()
                .productInfoId(1L)
                .brand(brand)
                .category(category)
                .name("Updated Product")
                .code("TP001-U")
                .releasePrice(BigDecimal.valueOf(12000))
                .releaseDate(LocalDateTime.now())
                .build();

        ProductOptionDto option = ProductOptionDto.builder()
                .productOptionValueId(1L)
                .groupName("Color")
                .value("Black")
                .build();
        ProductDto product = ProductDto.builder()
                .productId(1L)
                .inventory(150L)
                .options(List.of(option))
                .imageUrls(List.of("http://example.com/updated_image.jpg"))
                .build();

        return ProductResponseDto.builder()
                .productInfo(productInfo)
                .products(List.of(product))
                .build();
    }
}
