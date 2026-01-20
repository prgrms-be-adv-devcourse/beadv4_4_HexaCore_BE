package com.back.market.app;

import com.back.market.app.usecase.GetInstantPriceUseCase;
import com.back.market.app.usecase.MatchInstantTradeUseCase;
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
    private final MatchInstantTradeUseCase matchInstantTradeUseCase;

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
     * MARKET-004: 즉시 구매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantBuyPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantBuyPriceResponseDto getBuyNowPrice(Long productId) {
        return getInstantPriceUseCase.getBuyNowPrice(productId);
    }

    /**
     * MARKET-005: 즉시 판매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantSellPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantSellPriceResponseDto getSellNowPrice(Long productId) {
        return getInstantPriceUseCase.getSellNowPrice(productId);
    }

    /**
     * MARKET-009: 즉시 구매
     * @param buyerId 구매자 ID
     * @param requestDto BiddingRequestDto
     * @return 생성된 주문(Order)의 ID
     */
    public Long purchaseNow(Long buyerId, BiddingRequestDto requestDto) {
        return matchInstantTradeUseCase.buyNow(buyerId, requestDto);
    }

    /**
     * MARKET-011: 즉시 판매
     * @param sellerId 판매자 ID
     * @param requestDto BiddingRequestDto
     * @return 생성된 주문(Order)의 ID
     */
    public Long sellNow(Long sellerId, BiddingRequestDto requestDto) {
        return matchInstantTradeUseCase.sellNow(sellerId, requestDto);
    }

}
