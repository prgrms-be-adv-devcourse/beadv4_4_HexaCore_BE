package com.back.product.adapter.out.persistence;

import com.back.product.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsBrandByNameIgnoreCase(String name);

    @Query("""
                SELECT b FROM Brand b
                WHERE LOWER(b.name) IN LOWER(:brandNames)
            """)
    List<Brand> findAllByNameIgnoreCaseIn(@Param("brandNames") List<String> brandNames);
}
