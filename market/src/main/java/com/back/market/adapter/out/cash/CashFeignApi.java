package com.back.market.adapter.out.cash;

import com.back.common.response.CommonResponse;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.request.PaymentCancelRequestDto;
import com.back.market.dto.response.PayAndHoldResponseDto;
import com.back.market.dto.response.PaymentCancelResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cash-client", url = "${feign.cash.url}")
public interface CashFeignApi {
    @PostMapping("/api/v1/cash/payments")
    CommonResponse<PayAndHoldResponseDto> requestBidHold(@RequestBody PayAndHoldRequestDto requestDto);

    @PostMapping("/api/v1/internal/cash/payments/cancel")
    CommonResponse<PaymentCancelResponseDto> refundBidHold(@RequestBody PaymentCancelRequestDto requestDto);
}
