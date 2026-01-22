package com.back.product.adapter.out;

import com.back.product.domain.Brand;
import com.back.product.domain.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {
    boolean existsProductInfoByBrandAndProductCodeIgnoreCase(Brand brand, String code);
}
