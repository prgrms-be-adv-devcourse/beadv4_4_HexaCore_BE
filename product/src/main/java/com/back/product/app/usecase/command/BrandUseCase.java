package com.back.product.app.usecase.command;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.exception.InvalidValueException;
import com.back.product.adapter.out.persistence.BrandRepository;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.Brand;
import com.back.product.dto.command.BrandDataCommand;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.dto.request.BrandListCreateRequestDto;
import com.back.product.dto.model.BrandDto;
import com.back.product.mapper.BrandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<BrandDto> createBrands(List<BrandDataCommand> brands) {
        Map<String, Brand> existsBrands = productSupport.getAllBrands().stream()
                .collect(Collectors.toMap(
                        brand -> toPlainText(brand.getName()),
                        brand -> brand)
                );

        List<Brand> brandsToCreate = brands.stream()
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
    public BrandDto modifyBrand(Long brandId, BrandDataCommand brand) {
        Brand brandToModify = findBrandExists(brandId);

        String newName = toPlainText(brand.name());

        productSupport.getAllBrands().stream()
                .filter(existsBrand -> !existsBrand.getId().equals(brandId))
                .map(existsBrand -> toPlainText(existsBrand.getName()))
                .filter(existsBrandPlainName -> existsBrandPlainName.equals(newName))
                .findFirst()
                .ifPresent(_ -> { throw new CustomException(FailureCode.BRAND_NAME_DUPLICATE); });

        brandToModify.modifyName(brand.name());

        brandToModify.modifyImageUrl(brand.imageUrl());

        return brandMapper.toDto(brandToModify);
    }

    @Transactional
    public void deleteBrand(Long brandId) {
        brandRepository.deleteById(brandId);
    }

    private String toPlainText(String text) {
        if (text == null) {
            throw new InvalidValueException();
        }

        return  text.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
