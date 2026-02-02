package com.back.settlement.adapter.out.feign.market;

import com.back.common.dto.settlement.SettlementTargetOrder;

import java.time.LocalDate;
import java.util.List;

public interface OrderClient {
    List<SettlementTargetOrder> findSettlementTargetOrders(LocalDate targetDate, int page, int size);
}