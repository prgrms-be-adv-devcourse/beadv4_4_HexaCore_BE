package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.event.BrandSpringEventPublisher;
import com.back.product.app.usecase.BrandUseCase;
import com.back.product.app.usecase.ProductInfoUseCase;
import com.back.product.dto.command.BrandDataCommand;
import com.back.product.dto.model.BrandDto;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.dto.request.BrandListCreateRequestDto;
import com.back.product.dto.response.BrandListResponseDto;
import com.back.product.dto.response.BrandPageResponseDto;
import com.back.product.dto.response.BrandResponseDto;
import com.back.product.mapper.BrandDataCommandMapper;
import com.back.product.mapper.BrandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandFacade {
    private final BrandSpringEventPublisher brandSpringEventPublisher;
    private final BrandUseCase brandUseCase;
    private final BrandMapper brandMapper;
    private final ProductInfoUseCase productInfoUseCase;
    private final BrandDataCommandMapper brandDataCommandMapper;

    @Loggable
    @Transactional(readOnly = true)
    public BrandPageResponseDto getBrands(Integer page, Integer size) {
        Page<BrandDto> brands = brandUseCase.getBrands(page, size);
        return brandMapper.toPageResponseDto(
                brands.getContent(),
                brands.getTotalPages(),
                brands.getTotalElements(),
                brands.getNumber()
        );
    }

    @Loggable
    @Transactional
    public BrandListResponseDto createBrands(@Valid BrandListCreateRequestDto request) {
        List<BrandDataCommand> brandsCommands = request.brands().stream().map(brandDataCommandMapper::toCommand).toList();
        List<BrandDto> brandDtos = brandUseCase.createBrands(brandsCommands);
        brandSpringEventPublisher.sendCreatedEvent(brandDtos);
        return brandMapper.toListResponseDto(brandDtos);
    }

    @Loggable
    @Transactional
    public BrandResponseDto modifyBrand(Long brandId, @Valid BrandDataRequestDto request) {
        BrandDataCommand brandCommand = brandDataCommandMapper.toCommand(request);
        BrandDto brandDto = brandUseCase.modifyBrand(brandId, brandCommand);
        brandSpringEventPublisher.sendUpdatedEvent(brandDto);
        return brandMapper.toResponseDto(brandDto);
    }

    @Loggable
    @Transactional
    public void deleteBrand(Long brandId) {
        Boolean isUsed = productInfoUseCase.isBrandInUse(brandId);

        if (isUsed) {
            throw new CustomException(FailureCode.BRAND_IN_USE);
        }

        brandUseCase.deleteBrand(brandId);

        brandSpringEventPublisher.sendDeletedEvent(brandId);
    }
}
