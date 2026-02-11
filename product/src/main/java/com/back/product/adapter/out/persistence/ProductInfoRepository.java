package com.back.product.adapter.out.persistence;

import com.back.product.domain.Brand;
import com.back.product.domain.ProductInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {
    @Override
    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<ProductInfo> findById(Long productInfoId);

    boolean existsProductInfoByBrandAndProductCodeIgnoreCase(Brand brand, String code);

    Boolean existsByBrand_Id(Long brandId);

    Boolean existsByCategory_Id(Long categoryId);
}