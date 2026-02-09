package com.back.product.mapper;

import com.back.common.product.event.payload.BrandPayload;
import com.back.product.document.ProductDocument;
import com.back.product.domain.Brand;
import com.back.product.dto.command.BrandDataCommand;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.dto.model.BrandDto;
import com.back.product.dto.response.BrandListResponseDto;
import com.back.product.dto.response.BrandResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrandMapper {
    public BrandDto toDto(Brand brand) {
        return BrandDto.builder()
                .brandId(brand.getId())
                .name(brand.getName())
                .imageUrl(brand.getImageUrl())
                .build();
    }

    public BrandDto toDto(BrandPayload payload) {
        return BrandDto.builder()
                .brandId(payload.brandId())
                .name(payload.name())
                .build();
    }

    public Brand toEntity(BrandDataCommand brandDataCommand) {
        return Brand.builder()
                .name(brandDataCommand.name())
                .imageUrl(brandDataCommand.imageUrl())
                .build();
    }

    public ProductDocument.Brand toDocument(BrandDto brandDto) {
        return ProductDocument.Brand.builder()
                .brandId(brandDto.brandId())
                .brandName(brandDto.name())
                .build();
    }

    public BrandResponseDto toResponseDto(BrandDto brandDto) {
        return BrandResponseDto.builder()
                .brand(brandDto)
                .build();
    }

    public BrandListResponseDto toListResponseDto(List<BrandDto> brandDtos) {
        return BrandListResponseDto.builder()
                .brands(brandDtos)
                .build();
    }

    public BrandPayload toPayload(BrandDto brandDto) {
        return new BrandPayload(
                brandDto.brandId(),
                brandDto.name()
        );
    }
}
