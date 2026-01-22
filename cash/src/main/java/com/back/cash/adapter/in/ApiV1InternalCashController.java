package com.back.cash.adapter.in;

import com.back.cash.app.CashFacade;
import com.back.cash.dto.request.PaymentCancelRequestDto;
import com.back.cash.dto.response.PaymentCancelResponseDto;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/cash/payments")
@RequiredArgsConstructor
public class ApiV1InternalCashController {

    private final CashFacade cashFacade;

    @PostMapping("/cancel")
    public ResponseEntity<CommonResponse<PaymentCancelResponseDto>> cancel(@RequestBody PaymentCancelRequestDto req) {
        PaymentCancelResponseDto result = cashFacade.cancelPayment(req);

        return ResponseEntity
                .status(SuccessCode.OK.getHttpStatus())
                .body(CommonResponse.success(SuccessCode.OK, result));
    }
}
