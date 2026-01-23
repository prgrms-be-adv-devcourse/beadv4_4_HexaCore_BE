package com.back.cash.adapter.in;

import com.back.cash.app.CashPayoutFacade;
import com.back.common.code.SuccessCode;
import com.back.common.dto.settlement.SettlementPayoutRequest;
import com.back.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class ApiV1InternalPayoutController {
    private final CashPayoutFacade cashPayoutFacade;

    @PostMapping("/payout")
    public CommonResponse<Void> requestPayout(@RequestBody List<SettlementPayoutRequest> requests) {
        cashPayoutFacade.requestPayout(requests);
        return CommonResponse.success(SuccessCode.OK, null);
    }
}
