package com.back.market.mapper;

import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.dto.request.BiddingRequestDto;
import org.springframework.stereotype.Component;

@Component
public class BiddingMapper {
    public Bidding toEntity(BiddingRequestDto requestDto, MarketUser user, MarketProduct product, BiddingPosition position) {
        return Bidding.builder()
                .marketUser(user)
                .marketProduct(product)
                .price(requestDto.getPrice())
                .position(position)
                .status(BiddingStatus.PROCESS)
                .build();
    }
}
