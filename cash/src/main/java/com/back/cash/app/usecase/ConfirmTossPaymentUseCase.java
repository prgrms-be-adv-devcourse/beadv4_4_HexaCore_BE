package com.back.cash.app.usecase;

import com.back.cash.adapter.out.TossPaymentsClient;
import com.back.cash.app.ConfirmPaymentSupport;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmTossPaymentUseCase {

    private final ConfirmPaymentSupport confirmPaymentSupport;
    private final TossPaymentsClient tossPaymentsClient;

    /**
     * 토스 결제 확인
     */
    public ConfirmResultResponseDto execute(TossConfirmRequest req) {
        // 검증 (이미 DONE이면 바로 반환)
        ConfirmResultResponseDto alreadyDone = confirmPaymentSupport.validatePayment(req);
        if (alreadyDone != null) {
            return alreadyDone;
        }

        // 토스 confirm 호출
        TossConfirmResult tossResult = callTossConfirm(req);

        // 결과 반영
        return switch (tossResult.status()) {
            case SUCCESS -> confirmPaymentSupport.applySuccess(req.orderId(), req.paymentKey());
            case FAIL -> confirmPaymentSupport.applyFailure(req.orderId(), tossResult.failReason());
            case UNKNOWN -> {
                log.warn("[TOSS_CONFIRM_UNKNOWN] orderId={} - 결제 상태 불확실", req.orderId());
                yield ConfirmResultResponseDto.pending();
            }
        };
    }

    /**
     * 토스 confirm API 호출
     *
     * ResourceAccessException(타임아웃/네트워크 오류) → UNKNOWN: 토스 처리 여부 불확실
     * 그 외 예외(HTTP 4xx/5xx 등) → FAIL: 토스가 명시적으로 거부
     */
    private TossConfirmResult callTossConfirm(TossConfirmRequest req) {
        try {
            tossPaymentsClient.confirm(req.paymentKey(), req.orderId(), req.amount());
            log.info("[TOSS_CONFIRM_SUCCESS] orderId={}, paymentKey={}, amount={}",
                    req.orderId(), req.paymentKey(), req.amount());
            return TossConfirmResult.success();
        } catch (ResourceAccessException e) {
            log.error("[TOSS_CONFIRM_UNKNOWN] orderId={}, paymentKey={}, error={}",
                    req.orderId(), req.paymentKey(), e.getMessage(), e);
            return TossConfirmResult.unknown();
        } catch (Exception e) {
            log.error("[TOSS_CONFIRM_FAIL] orderId={}, paymentKey={}, amount={}, error={}",
                    req.orderId(), req.paymentKey(), req.amount(), e.getMessage(), e);
            return TossConfirmResult.fail(e.getMessage());
        }
    }

    private enum TossConfirmStatus { SUCCESS, FAIL, UNKNOWN }

    private record TossConfirmResult(TossConfirmStatus status, String failReason) {
        static TossConfirmResult success() { return new TossConfirmResult(TossConfirmStatus.SUCCESS, null); }
        static TossConfirmResult fail(String reason) { return new TossConfirmResult(TossConfirmStatus.FAIL, reason); }
        static TossConfirmResult unknown() { return new TossConfirmResult(TossConfirmStatus.UNKNOWN, null); }
    }

}
