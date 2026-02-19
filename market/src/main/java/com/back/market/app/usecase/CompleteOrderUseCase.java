package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
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
        Order order = marketSupport.findOrderById(orderId);

        if (!order.getBuyBidding().getMarketUser().getId().equals(userId)) {
            throw new CustomException(FailureCode.ORDER_ACCESS_DENIED);
        }
        if(order.getOrderStatus() != OrderStatus.DELIVERY_COMPLETED) {
            throw new CustomException(FailureCode.ORDER_NOT_DELIVERED);
        }
        order.changeStatus(OrderStatus.COMPLETED);
        return order;
    }
}
