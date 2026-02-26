package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.domain.Order;
import com.back.market.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusService {
    private final OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markStatusInNewTx(Long orderId, OrderStatus status) {
        log.info("[OrderStatusService] markStatusInNewTx 시작 - orderId={}, newStatus={}", orderId, status);
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BadRequestException(FailureCode.ORDER_NOT_FOUND));

            order.changeStatus(status);
            orderRepository.save(order);
            log.info("[OrderStatusService] markStatusInNewTx 커밋됨 - orderId={}, newStatus={}", orderId, status);
        } catch (Exception e) {
            log.error("[OrderStatusService] markStatusInNewTx 실패 - orderId={}, newStatus={}, error={}", orderId, status, e.getMessage(), e);
            throw e;
        }
    }
}
