package com.back.product.adapter.out.persistence;

import com.back.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsCategoryByNameIgnoreCase(String name);

    @Query("""
                SELECT c FROM Category c
                WHERE LOWER(c.name) IN :categoryNames
            """)
    List<Category> findAllByNameIgnoreCaseIn(@Param("categoryNames") List<String> categoryNames);
}
