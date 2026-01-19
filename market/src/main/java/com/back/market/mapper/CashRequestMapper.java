package com.back.market.mapper;

import com.back.market.dto.enums.RelType;
import com.back.market.dto.request.PayAndHoldRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CashRequestMapper {
    //구매 입찰용 요청 생성
    public PayAndHoldRequestDto toPayAndHoldRequestForBidding(Long userId, BigDecimal price, Long biddingId) {
        return new PayAndHoldRequestDto(
                userId,
                price,
                "구매 입찰 포인트 충전",
                RelType.BIDDING,
                biddingId
        );
    }

    //주문용 요청 생성
    public PayAndHoldRequestDto toPayAndHoldRequestForOrder(Long userId, BigDecimal totalPrice, String productName, Long orderId) {
        return new PayAndHoldRequestDto(
                userId,
                totalPrice,
                productName,
                RelType.ORDER,
                orderId
        );
    }
}
