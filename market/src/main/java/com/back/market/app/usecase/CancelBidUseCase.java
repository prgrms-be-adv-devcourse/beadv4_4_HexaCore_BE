package com.back.market.app.usecase;

import com.back.market.adapter.out.BiddingRepository;
import com.back.market.app.MarketSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelBidUseCase {
    private final BiddingRepository biddingRepository;
    private final MarketSupport marketSupport;
}
