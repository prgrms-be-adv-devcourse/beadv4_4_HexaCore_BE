package com.back.market.app;

import com.back.market.app.usecase.RegisterBidUseCase;
import com.back.market.dto.request.BiddingRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketFacade {
    private final RegisterBidUseCase registerBidUseCase;

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
}
