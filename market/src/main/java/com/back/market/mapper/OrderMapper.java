package com.back.market.mapper;

import com.back.market.domain.Bidding;
import com.back.market.domain.Order;
import com.back.market.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderMapper {
    public Order toEntity(Bidding buyBid, Bidding sellBid, String address){
        return Order.builder()
                .buyBidding(buyBid)
                .sellBidding(sellBid)
                .price(sellBid.getPrice())
                .address(address)
                .orderStatus(OrderStatus.HOLD) //초기 상태 설정
                .requestPaymentDate(LocalDateTime.now())
                .build();
    }
}
