package com.back.cash.adapter.in;

import com.back.cash.app.CashFacade;
import com.back.cash.dto.request.PayAndHoldRequestDto;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.request.TossFailRequestDto;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import com.back.cash.dto.response.PayAndHoldResponseDto;
import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cash/payments")
@RequiredArgsConstructor
@Slf4j
public class ApiV1CashController {
    private final CashFacade cashFacade;

    @PostMapping
    public PayAndHoldResponseDto payAndHold(@RequestBody PayAndHoldRequestDto dto) {
        return cashFacade.payAndHold(dto);
    }

    @PostMapping("/confirm/toss")
    public ResponseEntity<CommonResponse<?>> confirm(@RequestBody TossConfirmRequest req) {

        ConfirmResultResponseDto result = cashFacade.confirmTossPayment(req);

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    CommonResponse.createError(
                            FailureCode.PAYMENT_CONFIRM_FAILED.getHttpStatus(),
                            FailureCode.PAYMENT_CONFIRM_FAILED.getCode(),
                            FailureCode.PAYMENT_CONFIRM_FAILED.getMessage(),
                            result.failedDto()
                    )
            );
        }

        return ResponseEntity.ok(CommonResponse.success(SuccessCode.OK, result.completedDto()));
    }

    @PostMapping("/fail")
    public ResponseEntity<CommonResponse<?>> fail(@RequestBody TossFailRequestDto req) {
        log.info("[TOSS_FAIL] orderId={}, code={}, message={}", req.orderId(), req.code(), req.message());
        cashFacade.failTossPayment(req);
        return ResponseEntity.ok(CommonResponse.success(SuccessCode.OK, null));
    }

}
