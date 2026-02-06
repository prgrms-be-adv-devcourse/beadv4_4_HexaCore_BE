package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Order;
import com.back.market.dto.response.OrderDetailResponseDto;
import com.back.market.dto.response.OrderListResponseDto;
import com.back.market.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetOrdersUseCase {
    private final MarketSupport marketSupport;
    private final OrderMapper orderMapper;

    /**
     * 구매 내역 목록 조회
     * @param userId 사용자 ID
     * @param pageable 페이징
     * @return Page<OrderListResponseDto>
     */
    public Page<OrderListResponseDto> getBuyingList(Long userId, Pageable pageable) {
        Page<Order> orders = marketSupport.findBuyHistoryList(userId, pageable);
        return orders.map(orderMapper::toOrderListResponseDto);
    }

    /**
     * 판매 내역 목록 조회
     * @param userId 사용자 ID
     * @param pageable 페이징
     * @return Page<OrderListResponseDto>
     */
    public Page<OrderListResponseDto> getSellingList(Long userId, Pageable pageable) {
        Page<Order> orders = marketSupport.findSellHistoryList(userId, pageable);
        return orders.map(orderMapper::toOrderListResponseDto);
    }

    /**
     * 주문 상세 내역 조회
     * @param userId 사용자 ID
     * @param orderId 주문 ID
     * @return OrderDetailResponseDto
     */
    public OrderDetailResponseDto getOrderDetail(Long userId, Long orderId) {

        Order order = marketSupport.findOrderWithDetails(userId, orderId);
        return orderMapper.toOrderDetailResponseDto(order);
    }

}
