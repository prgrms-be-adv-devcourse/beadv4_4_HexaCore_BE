package com.back.product.mapper;

import com.back.product.domain.Product;
import com.back.product.domain.ProductImage;
import com.back.product.domain.ProductInfo;
import com.back.product.domain.ProductOptionValues;
import com.back.product.dto.ProductDto;
import com.back.product.dto.ProductOptionDto;
import com.back.product.dto.request.ProductVariantCreateRequestDto;
import com.back.product.dto.request.ProductVariantUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final ProductOptionMapper productOptionMapper;
    private final ProductImageMapper productImageMapper;

    public Product toEntity(ProductInfo productInfo, Long inventory) {
        return Product.builder()
                .productInfo(productInfo)
                .inventory(inventory)
                .build();
    }

    public ProductDto toDto(Product product, List<ProductOptionValues> options, List<ProductImage> images) {
        List<ProductOptionDto> optionDtos = options.stream().map(productOptionMapper::toDto).toList();
        List<String> imageUrlDtos = images.stream().map(productImageMapper::toDto).toList();

        return ProductDto.builder()
                .productId(product.getId())
                .inventory(product.getInventory())
                .options(optionDtos)
                .imageUrls(imageUrlDtos)
                .build();
    }
}
