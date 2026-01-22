package com.back.product.adapter.out;

import com.back.product.domain.Product;
import com.back.product.domain.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByProductInfo(ProductInfo productInfo);
}
