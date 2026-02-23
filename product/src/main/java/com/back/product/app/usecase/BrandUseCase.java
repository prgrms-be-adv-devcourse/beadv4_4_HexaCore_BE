package com.back.product.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.exception.InvalidValueException;
import com.back.product.adapter.out.persistence.BrandRepository;
import com.back.product.domain.Brand;
import com.back.product.dto.command.BrandDataCommand;
import com.back.product.dto.model.BrandDto;
import com.back.product.mapper.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Loggable
    @Transactional(readOnly = true)
    public Page<BrandDto> getBrands(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return productSupport.getAllBrands(pageable).map(brandMapper::toDto);
    }

    @Loggable
    @Transactional
    public List<BrandDto> createBrands(List<BrandDataCommand> brands) {
        List<String> newBrandNames = brands.stream().map(brand -> brand.name().toLowerCase()).toList();

        Map<String, Brand> existsBrands = productSupport.getAllBrandsByName(newBrandNames).stream()
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

    @Loggable
    @Transactional(readOnly = true)
    public Brand findBrandExists(Long brandId) {
        return productSupport.findBrandById(brandId)
                .orElseThrow(() -> new CustomException(FailureCode.BRAND_NOT_FOUND));
    }

    @Loggable
    @Transactional
    public BrandDto modifyBrand(Long brandId, BrandDataCommand brand) {
        Brand brandToModify = findBrandExists(brandId);

        String newName = brand.name().toLowerCase();

        if (productSupport.existsBrandByNameAndIdNot(newName, brandToModify.getId())) {
            throw new CustomException(FailureCode.BRAND_NAME_DUPLICATE);
        }

        brandToModify.modifyName(brand.name());

        brandToModify.modifyImageUrl(brand.imageUrl());

        return brandMapper.toDto(brandToModify);
    }

    @Loggable
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
