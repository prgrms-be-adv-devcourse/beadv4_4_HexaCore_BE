package com.back.product.mapper;

import com.back.product.domain.ProductInfo;
import com.back.product.dto.command.ProductInfoDataCommand;
import com.back.product.event.kafka.ProductInfoPayload;
import com.back.product.dto.model.ProductInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductInfoMapper {
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;

    public ProductInfo toEntity(ProductInfoDataCommand command) {
        return ProductInfo.builder()
                .brand(command.brand())
                .category(command.category())
                .name(command.name())
                .productCode(command.code())
                .releasePrice(command.releasePrice())
                .releasedDate(command.releasedDate())
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

    public ProductInfoDto toDto(ProductInfoPayload payload) {
        return ProductInfoDto.builder()
                .productInfoId(payload.productInfoId())
                .brand(brandMapper.toDto(payload.brand()))
                .category(categoryMapper.toDto(payload.category()))
                .name(payload.name())
                .code(payload.code())
                .releasePrice(payload.releasePrice())
                .releaseDate(payload.releaseDate())
                .build();
    }

    public ProductInfoPayload toPayload(ProductInfoDto productInfoDto) {
        return ProductInfoPayload.builder()
                .productInfoId(productInfoDto.productInfoId())
                .brand(brandMapper.toPayload(productInfoDto.brand()))
                .category(categoryMapper.toPayload(productInfoDto.category()))
                .name(productInfoDto.name())
                .code(productInfoDto.code())
                .releasePrice(productInfoDto.releasePrice())
                .releaseDate(productInfoDto.releaseDate())
                .build();
    }
}
