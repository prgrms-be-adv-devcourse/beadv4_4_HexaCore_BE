package com.back.market.adapter.out;

import com.back.market.domain.Order;
import com.back.market.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryCustom {
    // 구매 내역 조회
    Page<Order> findBuyHistoryList(Long userId, List<OrderStatus> statuses, Pageable pageable);

    // 판매 내역 조회
    Page<Order> findSellHistoryList(Long userId, List<OrderStatus> statuses, Pageable pageable);

    // 상세 조회
    Optional<Order> findOrderWithDetails(Long userId, Long orderId);
}
