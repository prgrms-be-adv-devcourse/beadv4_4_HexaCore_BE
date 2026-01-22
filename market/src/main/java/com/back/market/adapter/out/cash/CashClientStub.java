package com.back.market.adapter.out.cash;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.common.feign.cash.enums.PayAndHoldStatus;
import com.back.common.feign.cash.enums.RelType;
import com.back.common.feign.cash.request.PayAndHoldRequestDto;
import com.back.common.feign.cash.request.PaymentCancelRequestDto;
import com.back.common.feign.cash.response.PayAndHoldResponseDto;
import com.back.common.feign.cash.response.PaymentCancelResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Cash 모듈의 기능을 하는 가짜 클라이언트
 */
@Slf4j
@Component
@Profile("local") //local 환경일 때만 활성화
public class CashClientStub implements CashClient{
    /**
     * 결제 및 홀딩 요청 처리
     * @param requestDto 요청 dto, 구매자 ID, 금액, 참조 타입(ORDER/BIDDING), 참조 ID 포함
     * @return 결제 완료(PAID) 또는 PG 결제 필요(REQUIRES_PG) 응답
     */
    @Override
    public CommonResponse<PayAndHoldResponseDto> requestBidHold(PayAndHoldRequestDto requestDto) {
        String actionType = requestDto.relType() == RelType.BIDDING ? "입찰 홀딩" : "즉시 결제";
        log.info("[FakeCashClient] {} 요청 수신: {}", actionType, requestDto);

        Long userId = requestDto.buyerId();
        BigDecimal amount = requestDto.totalAmount();

        // 강제 실패 시뮬레이션 (5000원) -> 롤백 테스트용
        if (amount.intValue() == 5000) {
            log.warn("[FakeCashClient] 강제 실패 트리거 작동 (5000원)");
            return CommonResponse.createError(
                    HttpStatus.BAD_REQUEST,
                    FailureCode.WALLET_CHARGE_FAILED.getCode(),
                    "강제 결제 실패",
                    null
            );
        }

        //테스트용(9000원 요청시 잔액 부족 에러가 나도록)
        if (amount.intValue() == 9000) {
            log.info("[FakeCashClient] 예치금 부족 -> PG 결제 유도 (REQUIRES_PG) | RelId: {}", requestDto.relId());
            PayAndHoldResponseDto pgResponse = PayAndHoldResponseDto.of(
                    PayAndHoldStatus.REQUIRES_PG, // PG 결제 필요 상태
                    requestDto.relType(),         // 요청받은 RelType 유지
                    requestDto.relId(),           // 요청받은 RelId 유지
                    BigDecimal.ZERO,              // 예치금 사용액 0원
                    amount,     // 전액 PG 결제 필요
                    "toss-order-fake-9000"        // 가짜 토스 주문 ID 생성
            );

            return CommonResponse.success(SuccessCode.OK, pgResponse);
        }

        PayAndHoldResponseDto paidResponse = PayAndHoldResponseDto.of(
                PayAndHoldStatus.PAID,         // 즉시 결제 완료 상태
                requestDto.relType(),
                requestDto.relId(),
                amount,                        // 전액 예치금 사용
                BigDecimal.ZERO,               // PG 필요 금액 0원
                null                           // PG 정보 없음
        );

        log.info("[FakeCashClient] 결제/홀딩 완료 (PAID): User {}, Amount {}", userId, amount);

        return CommonResponse.success(SuccessCode.OK, paidResponse);
    }

    /**
     * 입찰 취소 시 예치금 환불(홀딩 해제) 요청 처리)
     * @param requestDto 요청 dto
     * @return 홀딩 해제 완료 응답
     */
    @Override
    public CommonResponse<PaymentCancelResponseDto> refundBidHold(PaymentCancelRequestDto requestDto) {
        log.info("[FakeCashClient] 환불(홀딩 해제) 요청 수신: {}", requestDto);

        // 테스트용 강제 실패 트리거 (7000원)
        if (requestDto.amount().intValue() == 7000) {
            log.warn("[FakeCashClient] 환불 강제 실패 트리거 작동 (7000원)");
            return CommonResponse.createError(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    FailureCode.WALLET_REFUND_FAILED.getCode(),
                    "Cash 모듈 환불 처리 중 오류 발생 (테스트)",
                    null
            );
        }

        // 환불 결과 DTO 생성
        // (환불이 완료되었다는 의미로 PAID 상태 사용, 실제 금액 차감은 없으므로 0원 처리)
        PaymentCancelResponseDto response = PaymentCancelResponseDto.of(
                requestDto.userId(),
                requestDto.amount()
        );

        log.info("[FakeCashClient] 예치금 홀딩 해제 완료 - User: {}, RelId: {}, Amount: {}", requestDto.userId(), requestDto.relId(), requestDto.amount());

        return CommonResponse.success(SuccessCode.OK, response);
    }
}
