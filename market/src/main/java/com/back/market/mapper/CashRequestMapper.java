package com.back.market.mapper;

import com.back.market.domain.Order;
import com.back.market.dto.enums.RelType;
import com.back.market.dto.request.PayAndHoldRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CashRequestMapper {
    //구매 입찰용 요청 생성
    public PayAndHoldRequestDto toPayAndHoldRequestForBidding(Long userId, BigDecimal price, Long biddingId) {
        return PayAndHoldRequestDto.of(
                userId,
                price,
                "구매 입찰 포인트 충전",
                RelType.BIDDING,
                biddingId
        );
    }

    //주문용 요청 생성
    public PayAndHoldRequestDto toPayAndHoldRequestForOrder(
            Order order
    //        Long userId, BigDecimal totalPrice, String productName, Long orderId
    ) {
        return PayAndHoldRequestDto.of(
                order.getBuyBidding().getMarketUser().getId(),
                order.getPrice(),
                order.getBuyBidding().getMarketProduct().getName(),
                RelType.ORDER,
                order.getId()
        );
    }
}
