package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.dto.request.PaymentCompletedRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Market Internal API", description = "내부 통신용 API")
@RequestMapping("/api/v1/market/internal")
public interface ApiV1MarketInternal {

    @Operation(summary = "결제 완료 통지 수신 (Internal Callback)", description = "Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정한다.")
    @PostMapping("/payment-confirm")
    CommonResponse<Void> confirmPayment(@RequestBody PaymentCompletedRequestDto requestDto);
}
