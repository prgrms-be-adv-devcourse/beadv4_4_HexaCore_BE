package com.back.product.mapper;

import com.back.product.domain.Brand;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.BrandDto;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {
    public BrandDto toDto(Brand brand) {
        return BrandDto.builder()
                .name(brand.getName())
                .logoUrl(brand.getImageUrl())
                .build();
    }

    public Brand toEntity(BrandCreateRequestDto request) {
        return Brand.builder()
                .name(request.name())
                .imageUrl(request.logoUrl())
                .build();
    }
}
