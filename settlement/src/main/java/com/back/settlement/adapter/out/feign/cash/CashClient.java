package com.back.settlement.adapter.out.feign.cash;

import com.back.common.dto.settlement.SettlementPayoutRequest;
import java.util.List;

public interface CashClient {
    void requestPayout(List<SettlementPayoutRequest> payoutRequests);
}
