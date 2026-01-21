package com.back.market.app;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import com.back.common.exception.BadRequestException;
import com.back.common.response.CommonResponse;
import com.back.market.adapter.out.client.CashClient;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.response.PayAndHoldResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketSupport {
    private final CashClient cashClient;

    public PayAndHoldResponseDto getPayAndHoldResult(PayAndHoldRequestDto paymentReq) {
        CommonResponse<PayAndHoldResponseDto> response = cashClient.requestBidHold(paymentReq);

        boolean isSuccess = response != null && SuccessCode.OK.getCode().equals(response.getCode());

        if (!isSuccess) {
            // 응답이 아예 없거나 실패한 경우
            if (response != null) {
                // [변경 포인트] isChargeFailed() 메서드가 없으므로 직접 코드 비교
                if (FailureCode.WALLET_CHARGE_FAILED.getCode().equals(response.getCode())) {
                    throw new BadRequestException(FailureCode.WALLET_CHARGE_FAILED);
                }
                // 그 외 실패 사유
                log.error("[MarketSupport] Cash 모듈 에러 - Code: {}, Msg: {}", response.getCode(), response.getMessage());
            } else {
                log.error("[MarketSupport] Cash 모듈 응답 없음 (Null)");
            }
            // 공통 에러 던지기
            throw new BadRequestException(FailureCode.CASH_MODULE_ERROR);
        }
        return response.getData();
    }

    public PayAndHoldResponseDto refundBidPayment(PayAndHoldRequestDto refundRequest) {
        // 1. 요청 전송
        CommonResponse<PayAndHoldResponseDto> response = cashClient.refundBidHold(refundRequest);

        // 2. 응답 검증
        boolean isSuccess = response != null &&
                SuccessCode.OK.getCode().equals(response.getCode());

        if (!isSuccess) {
            String msg = (response != null) ? response.getMessage() : "No Response";
            log.error("[MarketSupport] 환불 요청 실패 - User: {}, Reason: {}", refundRequest.buyerId(), msg);

            // 환불 실패 시 예외를 던져 트랜잭션 롤백 유도
            throw new BadRequestException(FailureCode.WALLET_CHARGE_FAILED);
        }

        log.info("[MarketSupport] 환불 요청 성공 - RelId: {}", refundRequest.relId());

        // 3. 데이터 반환 (.getData() 사용)
        return response.getData();
    }
}
