package com.back.market.app.usecase;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Order;
import com.back.market.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetSettlementDataUseCase {

    private final OrderMapper orderMapper;
    private final MarketSupport marketSupport;

    @Transactional(readOnly = true)
    public List<SettlementTargetOrder> getSettlementData(LocalDate targetDate, int page, int size) {
        // 날짜 범위 계산
        LocalDateTime startDateTime = targetDate.atStartOfDay();
        LocalDateTime endDateTime = targetDate.atTime(LocalTime.MAX);

        // 페이징
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        // DB 조회
        List<Order> orders = marketSupport.findSettlementTargetOrders(startDateTime, endDateTime, pageable);

        return orders.stream()
                .map(orderMapper::toSettlementTargetOrder)
                .collect(Collectors.toList());
    }
}
