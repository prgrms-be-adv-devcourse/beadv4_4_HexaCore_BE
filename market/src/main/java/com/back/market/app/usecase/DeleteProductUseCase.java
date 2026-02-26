package com.back.market.app.usecase;

import com.back.market.adapter.out.MarketProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteProductUseCase {
    private final MarketProductRepository marketProductRepository;

    public void deleteMarketProduct(Long productInfoId) {
        log.info("[DeleteProductUseCase] 상품 삭제 시작 - ProductInfoId: {}", productInfoId);
        marketProductRepository.deleteByProductInfoId(productInfoId);
        log.info("[DeleteProductUseCase] 상품 삭제 완료 - ProductInfoId: {}", productInfoId);
    }
}
