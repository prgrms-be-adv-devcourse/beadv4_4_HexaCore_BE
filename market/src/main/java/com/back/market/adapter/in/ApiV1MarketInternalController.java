package com.back.market.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.market.app.MarketInternalFacade;
import com.back.market.dto.request.PaymentCompletedRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiV1MarketInternalController implements ApiV1MarketInternal {

    private final MarketInternalFacade marketInternalFacade;

    @Override
    public CommonResponse<Void> confirmPayment(@RequestBody PaymentCompletedRequestDto requestDto) {
        boolean isSuccess = marketInternalFacade.confirmPayment(requestDto);
        if(!isSuccess) {
            return CommonResponse.success(SuccessCode.ALREADY_PROCESSED, null);
        }
        return CommonResponse.success(SuccessCode.OK, null);
    }
}
