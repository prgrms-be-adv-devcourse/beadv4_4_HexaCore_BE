package com.back.market.app.usecase;

import com.back.market.adapter.out.CartRepository;
import com.back.market.domain.Cart;
import com.back.market.domain.MarketUser;
import com.back.market.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCartUseCase {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    @Transactional
    public void createCart(MarketUser user) {
        if (cartRepository.existsByMarketUser(user)) {
            return;
        }

        Cart cart = cartMapper.toEntity(user);
        cartRepository.save(cart);
    }
}
