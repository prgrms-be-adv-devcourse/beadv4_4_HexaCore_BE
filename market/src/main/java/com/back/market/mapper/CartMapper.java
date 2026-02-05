package com.back.market.mapper;

import com.back.market.domain.Cart;
import com.back.market.domain.MarketUser;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {
    public Cart toEntity(MarketUser user) {
        return Cart.builder()
                .marketUser(user)
                .itemsCount(0)
                .build();
    }
}
