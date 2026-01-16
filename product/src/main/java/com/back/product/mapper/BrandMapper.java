package com.back.product.mapper;

import com.back.product.domain.Brand;
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
}
