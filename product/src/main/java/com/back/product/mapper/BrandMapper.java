package com.back.product.mapper;

import com.back.common.product.event.payload.BrandPayload;
import com.back.product.domain.Brand;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.dto.model.BrandDto;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {
    public BrandDto toDto(Brand brand) {
        return BrandDto.builder()
                .brandId(brand.getId())
                .name(brand.getName())
                .imageUrl(brand.getImageUrl())
                .build();
    }

    public Brand toEntity(BrandDataRequestDto request) {
        return Brand.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();
    }

    public BrandDto toDto(BrandPayload payload) {
        return BrandDto.builder()
                .brandId(payload.brandId())
                .name(payload.name())
                .build();
    }
}
