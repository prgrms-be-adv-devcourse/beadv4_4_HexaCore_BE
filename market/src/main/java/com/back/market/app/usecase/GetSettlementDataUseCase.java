package com.back.market.app.usecase;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.domain.Order;
import com.back.market.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetSettlementDataUseCase {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<SettlementTargetOrder> getSettlementData(YearMonth targetMonth, int page, int size) {
        // 날짜 범위 계산
        LocalDateTime startDateTime = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = targetMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // 페이징
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        // DB 조회
        List<Order> orders = orderRepository.findSettlementTargetOrders(startDateTime, endDateTime, pageable);

        return orders.stream()
                .map(orderMapper::toSettlementTargetOrder)
                .collect(Collectors.toList());
    }
}
