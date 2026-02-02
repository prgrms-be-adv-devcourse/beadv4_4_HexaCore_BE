package com.back.settlement.adapter.out.feign.market;

import com.back.common.dto.settlement.SettlementTargetOrder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;
import java.util.List;

@HttpExchange
public interface OrderFeignClient extends OrderClient {

    @Override
    @GetExchange("/api/v1/internal/market/orders/settlement-target")
    List<SettlementTargetOrder> findSettlementTargetOrders(
            @RequestParam("targetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
