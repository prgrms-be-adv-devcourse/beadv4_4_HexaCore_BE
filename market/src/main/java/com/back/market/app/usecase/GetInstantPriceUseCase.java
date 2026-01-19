package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.domain.Bidding;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 즉시구매가, 즉시판매가 조회
 */
@Service
@RequiredArgsConstructor
public class GetInstantPriceUseCase {

    private final BiddingRepository biddingRepository;
    private final MarketProductRepository marketProductRepository;

    /**
     * 즉시 구매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantBuyPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantBuyPriceResponseDto getBuyNowPrice(Long productId) {
        // 상품 존재 여부 검증
        validateProductExists(productId);
        
        BigDecimal price = biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(
                productId,
                BiddingPosition.SELL,
                BiddingStatus.PROCESS
        ).map(Bidding::getPrice).orElse(null);

        return InstantBuyPriceResponseDto.of(productId, price);
    }

    /**
     * 즉시 판매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantSellPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantSellPriceResponseDto getSellNowPrice(Long productId) {
        //상품 존재 여부 검증
        validateProductExists(productId);
        BigDecimal price = biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(
                productId,
                BiddingPosition.BUY,
                BiddingStatus.PROCESS
        ).map(Bidding::getPrice).orElse(null);

        return InstantSellPriceResponseDto.of(productId, price);
    }

    private void validateProductExists(Long productId) {
        if(!marketProductRepository.existsById(productId)) {
            throw new BadRequestException(FailureCode.PRODUCT_NOT_FOUND);
        }
    }
}
