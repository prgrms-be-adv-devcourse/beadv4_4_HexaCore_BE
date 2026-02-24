package com.back.market.adapter.out;

import com.back.market.domain.MarketProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketProductRepository  extends JpaRepository<MarketProduct, Long> {
    boolean existsById(Long productOptionId);

    void deleteByProductInfoId(Long productInfoId);

    List<MarketProduct> findAllByProductInfoId(Long productInfoId);
}
