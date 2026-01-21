package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.app.MarketInternalFacade;
import com.back.market.dto.request.PaymentCompletedRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiV1MarketInternalController implements ApiV1MarketInternal {

    private final MarketInternalFacade marketInternalFacade;

    @Override
    public CommonResponse<Void> confirmPayment(PaymentCompletedRequestDto requestDto) {
        marketInternalFacade.confirmPayment(requestDto);
        return CommonResponse.successWithData(HttpStatus.OK, null);
    }
}
