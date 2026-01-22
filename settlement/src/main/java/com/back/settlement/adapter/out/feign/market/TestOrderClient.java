package com.back.settlement.adapter.out.feign.market;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.back.common.dto.settlement.SettlementTargetOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class TestOrderClient implements OrderClient {
    private final List<SettlementTargetOrder> orders = new ArrayList<>();

    @Override
    public List<SettlementTargetOrder> findSettlementTargetOrders(YearMonth targetMonth, int page, int size) {
        log.info("[TestOrderClient] 정산 대상 주문 조회. targetMonth={}, page={}, size={}", targetMonth, page, size);
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();
        List<SettlementTargetOrder> filtered = orders.stream()
                .filter(order -> {
                    LocalDate orderDate = order.confirmedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .toList();
        int fromIndex = page * size;
        if (fromIndex >= filtered.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<SettlementTargetOrder> result = filtered.subList(fromIndex, toIndex);
        log.info("[TestOrderClient] 조회된 주문 수: {}", result.size());
        return result;
    }

    public void addOrder(SettlementTargetOrder order) {
        orders.add(order);
    }

    public void clear() {
        orders.clear();
    }
}