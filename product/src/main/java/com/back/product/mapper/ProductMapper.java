package com.back.product.mapper;

import com.back.product.domain.Product;
import com.back.product.domain.ProductImage;
import com.back.product.domain.ProductInfo;
import com.back.product.domain.ProductOptionValues;
import com.back.product.dto.model.*;
import com.back.product.dto.response.ProductDetailListResponseDto;
import com.back.product.dto.response.ProductDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final OptionMapper optionMapper;
    private final ProductImageMapper productImageMapper;

    public Product toEntity(ProductInfo productInfo, Long inventory) {
        return Product.builder()
                .productInfo(productInfo)
                .inventory(inventory)
                .build();
    }

    public ProductDto toDto(Product product, List<ProductOptionValues> options, List<ProductImage> images) {
        List<OptionDto> optionDtos = optionMapper.toDtoList(options.stream().map(ProductOptionValues::getOptionValue).toList());
        List<String> imageUrlDtos = images.stream().map(productImageMapper::toDto).toList();

        return ProductDto.builder()
                .productId(product.getId())
                .inventory(product.getInventory())
                .options(optionDtos)
                .imageUrls(imageUrlDtos)
                .build();
    }

    public ProductDetailDto toDetailDto(ProductInfoDto productInfoDto, List<ProductDto> productDtos) {
        return ProductDetailDto.builder()
                .productInfo(productInfoDto)
                .products(productDtos)
                .build();
    }

    public ProductDetailResponseDto toDetailDto(ProductDetailDto productDetailDto) {
        return ProductDetailResponseDto.builder()
                .product(productDetailDto)
                .build();
    }

    public ProductDetailResponseDto toResponseDto(ProductInfoDto productInfoDto, List<ProductDto> productDtos) {
        ProductDetailDto productDetailDto = toDetailDto(productInfoDto, productDtos);

        return ProductDetailResponseDto.builder()
                .product(productDetailDto)
                .build();
    }

    public ProductDetailListResponseDto toListResponseDto(List<ProductDetailDto> productDetailDtos) {
        return ProductDetailListResponseDto.builder()
                .products(productDetailDtos)
                .build();
    }
}
