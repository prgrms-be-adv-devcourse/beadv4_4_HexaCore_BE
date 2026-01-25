package com.back.settlement.adapter.out.feign.cash;

import com.back.common.dto.settlement.SettlementPayoutRequest;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface CashFeignClient extends CashClient {

    @Override
    @PostExchange("/api/v1/cash/payout")
    void requestPayout(@RequestBody List<SettlementPayoutRequest> payoutRequests);
}
