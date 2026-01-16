package com.back.product.app.usecase;

import com.back.product.adapter.out.BrandRepository;
import com.back.product.domain.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSupport {
    private final BrandRepository brandRepository;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
}
