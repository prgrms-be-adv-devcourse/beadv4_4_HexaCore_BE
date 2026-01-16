package com.back.product.app;

import com.back.product.app.usecase.BrandUseCase;
import com.back.product.dto.BrandDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    private final BrandUseCase brandUseCase;

    @Transactional(readOnly = true)
    public List<BrandDto> getBrands() {
        return brandUseCase.getBrands();
    }
}
