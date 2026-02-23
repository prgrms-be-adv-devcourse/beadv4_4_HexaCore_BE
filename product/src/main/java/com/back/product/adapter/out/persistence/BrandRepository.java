package com.back.product.adapter.out.persistence;

import com.back.product.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query("""
                SELECT b FROM Brand b
                WHERE LOWER(b.name) IN :brandNames
            """)
    List<Brand> findAllByNameIgnoreCaseIn(@Param("brandNames") List<String> brandNames);

    boolean existsBrandByNameIgnoreCaseAndIdNot(String name, Long id);
}
