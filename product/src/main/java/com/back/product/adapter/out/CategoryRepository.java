package com.back.product.adapter.out;

import com.back.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsCategoryByNameIgnoreCase(String name);
}
