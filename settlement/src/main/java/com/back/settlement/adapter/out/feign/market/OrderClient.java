package com.back.settlement.adapter.out.feign.market;

import com.back.common.dto.settlement.SettlementTargetOrder;

import java.time.YearMonth;
import java.util.List;

public interface OrderClient {
    List<SettlementTargetOrder> findSettlementTargetOrders(YearMonth targetMonth, int page, int size);
}