package com.back.market.adapter.out;

import com.back.market.domain.Cart;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    boolean existsByMarketUser(MarketUser user);
}
