package com.back.product.adapter.out.persistence;

import com.back.product.domain.Product;
import com.back.product.domain.ProductInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"productInfo"})
    List<Product> findAllByProductInfo(ProductInfo productInfo);

    @EntityGraph(attributePaths = {"productInfo"})
    List<Product> findAllById(Iterable<Long> ids);
}
