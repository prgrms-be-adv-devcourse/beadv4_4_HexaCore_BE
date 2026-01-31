package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.exception.InvalidValueException;
import com.back.product.adapter.out.BrandRepository;
import com.back.product.domain.Brand;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.BrandDto;
import com.back.product.dto.request.BrandModifyRequestDto;
import com.back.product.dto.response.BrandListResponseDto;
import com.back.product.mapper.BrandMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<BrandDto> createBrands(@Valid BrandCreateRequestDto request) {
        Map<String, Brand> existsBrands = productSupport.getAllBrands().stream()
                .collect(Collectors.toMap(
                        brand -> toPlainText(brand.getName()),
                        brand -> brand)
                );

        List<Brand> brandsToCreate = request.brands().stream()
                .filter(newBrand -> {
                    String newName = toPlainText(newBrand.name());
                    return !existsBrands.containsKey(newName);
                })
                .map(brandMapper::toEntity).toList();

        List<Brand> createdBrands = brandRepository.saveAll(brandsToCreate);

        return createdBrands.stream().map(brandMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Brand findBrandExists(Long brandId) {
        return productSupport.findBrandById(brandId)
                .orElseThrow(() -> new CustomException(FailureCode.BRAND_NOT_FOUND));
    }

    @Transactional
    public BrandDto modifyBrand(Long brandId, @Valid BrandModifyRequestDto request) {
        Brand brandToModify = findBrandExists(brandId);

        String newName = toPlainText(request.name());

        productSupport.getAllBrands().stream()
                .filter(existsBrand -> !existsBrand.getId().equals(brandId))
                .map(existsBrand -> toPlainText(existsBrand.getName()))
                .filter(existsBrandPlainName -> existsBrandPlainName.equals(newName))
                .findFirst()
                .ifPresent(_ -> { throw new CustomException(FailureCode.BRAND_NAME_DUPLICATE); });

        brandToModify.modifyName(request.name());

        brandToModify.modifyImageUrl(request.imageUrl());

        return brandMapper.toDto(brandToModify);
    }

    private String toPlainText(String text) {
        if (text == null) {
            throw new InvalidValueException();
        }

        return  text.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
