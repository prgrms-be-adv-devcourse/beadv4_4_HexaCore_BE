package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.BrandRepository;
import com.back.product.domain.Brand;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.BrandDto;
import com.back.product.mapper.BrandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandUseCase {
    private final BrandMapper brandMapper;
    private final ProductSupport productSupport;
    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandDto> getBrands() {
        return productSupport.getAllBrands().stream().map(brandMapper::toDto).toList();
    }

    @Transactional
    public BrandDto createBrand(@Valid BrandCreateRequestDto request) {
        validateDuplicateBrandName(request.name());

        Brand brand = brandMapper.toEntity(request);

        Brand newBrand = brandRepository.save(brand);

        return brandMapper.toDto(newBrand);
    }

    private void validateDuplicateBrandName(String name) {
        if (productSupport.existsBrandByName(name)) {
            throw new CustomException(FailureCode.BRAND_NAME_DUPLICATE);
        }
    }
}
