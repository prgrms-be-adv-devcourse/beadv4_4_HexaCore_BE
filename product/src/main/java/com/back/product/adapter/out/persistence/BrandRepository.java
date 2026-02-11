package com.back.product.adapter.out.persistence;

import com.back.product.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsBrandByNameIgnoreCase(String name);
}
