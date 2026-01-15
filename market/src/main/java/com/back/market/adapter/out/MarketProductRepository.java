package com.back.market.adapter.out;

import com.back.market.domain.MarketProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketProductRepository  extends JpaRepository<MarketProduct, Long> {
}
