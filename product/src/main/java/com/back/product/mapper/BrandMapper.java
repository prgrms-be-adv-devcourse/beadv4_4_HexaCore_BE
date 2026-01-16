package com.back.product.mapper;

import com.back.product.domain.Brand;
import com.back.product.dto.response.BrandResponseDto;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {
    public BrandResponseDto toDto(Brand brand) {
        return BrandResponseDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logoUrl(brand.getImageUrl())
                .build();
    }
}
