package com.back.product.app.usecase;

import com.back.product.domain.Brand;
import com.back.product.mapper.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandUseCase {
    private final BrandMapper brandMapper;
    private final ProductSupport productSupport;

    @Transactional(readOnly = true)
    public List<BrandDto> getBrands() {
        return productSupport.getAllBrands().stream().map(brandMapper::toDto).toList();
    }
}
