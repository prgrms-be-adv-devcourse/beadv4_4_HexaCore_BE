package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
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

    public void createCart(MarketUser user) {
        // 장바구니 존재하는지 확인
        if (cartRepository.existsByMarketUser(user)) {
            log.info("[Market] 이미 장바구니가 존재하는 사용자입니다. id = {}", user.getId());
            return;
        }

        // 장바구니 생성
        try {
            Cart cart = cartMapper.toEntity(user);
            cartRepository.save(cart);
            log.info("[Market] 장바구니 생성 완료: userId = {}", user.getId());

        } catch (Exception e) {
            log.error("[Market] 장바구니 생성 중 오류 발생: userId = {}, error = {}", user.getId(), e.getMessage());
            throw new CustomException(FailureCode.INTERNAL_SERVER_ERROR);
        }
    }
}
