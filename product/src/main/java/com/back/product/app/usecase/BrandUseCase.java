package com.back.product.app.usecase;

import com.back.product.dto.response.BrandResponseDto;
import com.back.product.mapper.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandUseCase {
    private final BrandMapper brandMapper;
    private final ProductSupport productSupport;

    public List<BrandResponseDto> getBrands() {
        return productSupport.getAllBrands().stream().map(brandMapper::toDto).toList();
    }
}
