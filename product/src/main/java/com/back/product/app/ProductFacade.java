package com.back.product.app;

import com.back.product.app.usecase.BrandUseCase;
import com.back.product.dto.response.BrandResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    private final BrandUseCase brandUseCase;

    public List<BrandResponseDto> getBrands() {
        return brandUseCase.getBrands();
    }
}
