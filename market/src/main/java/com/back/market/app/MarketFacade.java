package com.back.market.app;

import com.back.market.app.usecase.GetInstantPriceUseCase;
import com.back.market.app.usecase.RegisterBidUseCase;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketFacade {
    private final RegisterBidUseCase registerBidUseCase;
    private final GetInstantPriceUseCase getInstantPriceUseCase;

    /**
     * MARKET-010: 구매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return 저장된 구매 입찰의 PK
     */
    @Transactional
    public Long registerBuyBid(Long userId, BiddingRequestDto requestDto) {
        return registerBidUseCase.registerBuyBid(userId, requestDto);
    }

    /**
     * MARKET-012: 판매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return 저장된 판매 입찰의 PK
     */
    @Transactional
    public Long registerSellBid(Long userId, BiddingRequestDto requestDto) {
        return registerBidUseCase.registerSellBid(userId, requestDto);
    }

    /**
     * 즉시 구매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantBuyPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantBuyPriceResponseDto getBuyNowPrice(Long productId) {
        return getInstantPriceUseCase.getBuyNowPrice(productId);
    }

    /**
     * 즉시 판매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantSellPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantSellPriceResponseDto getSellNowPrice(Long productId) {
        return getInstantPriceUseCase.getSellNowPrice(productId);
    }

}
