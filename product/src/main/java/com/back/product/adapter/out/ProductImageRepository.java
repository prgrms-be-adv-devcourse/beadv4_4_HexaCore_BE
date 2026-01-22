package com.back.product.adapter.out;

import com.back.product.domain.Product;
import com.back.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @Modifying
    @Query("update ProductImage pi set pi.deletedAt = now() where pi.product in :products")
    void deleteAllByProductIn(@Param("products") List<Product> existsProducts);

    List<ProductImage> findALlByProductIn(List<Product> products);
}
