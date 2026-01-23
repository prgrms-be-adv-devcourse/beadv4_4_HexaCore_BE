package com.back.market.mapper;

import com.back.common.dto.settlement.SettlementTargetOrder;
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

    public SettlementTargetOrder toSettlementTargetOrder(Order order) {
        Bidding sellBid = order.getSellBidding();
        Bidding buyBid = order.getBuyBidding();
        return new SettlementTargetOrder(
                order.getId(),                          // 주문 ID
                sellBid.getMarketProduct().getId(),     // 상품 ID(판매입찰 기준)
                buyBid.getMarketUser().getId(),         // 구매자 ID
                sellBid.getMarketUser().getId(),        // 판매자 ID
                sellBid.getMarketUser().getNickname(),  // 판매자 이름(닉네임)
                order.getPrice(),                       // 주문 가격
                order.getLastModifiedAt()               // 구매 확정 일시(마지막으로 수정된 날짜)
        );
    }
}
