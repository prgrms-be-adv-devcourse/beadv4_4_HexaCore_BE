package com.back.market.app.usecase;

import com.back.market.app.MarketSupport;
import com.back.market.domain.Order;
import com.back.market.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteOrderUseCase {

    private final MarketSupport marketSupport;

    @Transactional
    public Order completeOrder(Long userId, Long orderId) {
        Order order = marketSupport.findOrderById(orderId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (!order.getBuyBidding().getMarketUser().getId().equals(userId)) {
            throw new IllegalStateException("구매 확정 권한이 없습니다.");
        }
        if(order.getOrderStatus() != OrderStatus.DELIVERY_COMPLETED) {
            throw new IllegalStateException("배송 완료된 주문만 구매 확정할 수 있습니다.");
        }
        order.changeStatus(OrderStatus.COMPLETED);
        return order;
    }
}
