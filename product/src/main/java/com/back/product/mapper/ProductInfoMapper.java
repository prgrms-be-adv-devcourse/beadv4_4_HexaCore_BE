package com.back.product.mapper;

import com.back.product.domain.Brand;
import com.back.product.domain.Category;
import com.back.product.domain.ProductInfo;
import com.back.product.dto.ProductInfoDto;
import com.back.product.dto.request.ProductInfoCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductInfoMapper {
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;

    public ProductInfo toEntity(Brand brand, Category category, ProductInfoCreateRequestDto request) {
        return ProductInfo.builder()
                .brand(brand)
                .category(category)
                .name(request.name())
                .productCode(request.code())
                .releasePrice(request.releasePrice())
                .releasedDate(request.releasedDate())
                .build();
    }

    public ProductInfoDto toDto(ProductInfo productInfo) {
        return ProductInfoDto.builder()
                .productInfoId(productInfo.getId())
                .brand(brandMapper.toDto(productInfo.getBrand()))
                .category(categoryMapper.toDto(productInfo.getCategory()))
                .name(productInfo.getName())
                .code(productInfo.getProductCode())
                .releasePrice(productInfo.getReleasePrice())
                .releaseDate(productInfo.getReleasedDate())
                .build();
    }
}
