package com.back.market.adapter.out.cash;

import com.back.common.response.CommonResponse;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.request.PaymentCancelRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cash-client", url = "${feign.cash.url}")
public interface CashFeignApi {
    @PostMapping("/api/v1/internal/cash/payments")
    CommonResponse<PayAndHoldResponseDto> requestBidHold(@RequestBody PayAndHoldRequestDto requestDto);

    @PostMapping("/api/v1/internal/cash/payments/cancel")
    CommonResponse<PaymentCancelResponseDto> refundBidHold(@RequestBody PaymentCancelRequestDto requestDto);
}
