package com.back.market.adapter.out.client;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import com.back.market.app.port.out.CashClient;
import com.back.market.dto.enums.PayAndHoldStatus;
import com.back.market.dto.enums.RelType;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.response.CashApiResponse;
import com.back.market.dto.response.PayAndHoldResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Cash 모듈의 기능을 하는 가짜 클라이언트
 */
@Slf4j
@Component
public class FakeCashClient implements CashClient {
    /**
     * 결제 및 홀딩 요청 처리
     * @param requestDto 요청 dto, 구매자 ID, 금액, 참조 타입(ORDER/BIDDING), 참조 ID 포함
     * @return 결제 완료(PAID) 또는 PG 결제 필요(REQUIRES_PG) 응답
     */
    @Override
    public CashApiResponse<PayAndHoldResponseDto> requestBidHold(PayAndHoldRequestDto requestDto) {
        String actionType = requestDto.relType() == RelType.BIDDING ? "입찰 홀딩" : "즉시 결제";
        log.info("[FakeCashClient] {} 요청 수신: {}", actionType, requestDto);

        Long userId = requestDto.buyerId();
        BigDecimal amount = requestDto.totalAmount();

        // 강제 실패 시뮬레이션 (5000원) -> 롤백 테스트용
        if (amount.intValue() == 5000) {
            log.warn("[FakeCashClient] 강제 실패 트리거 작동 (5000원)");
            return CashApiResponse.<PayAndHoldResponseDto>builder()
                    .code(FailureCode.WALLET_CHARGE_FAILED.getCode()) // 실패 코드 (400)
                    .message("강제 결제 실패")
                    .build(); // success = false
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

            return CashApiResponse.<PayAndHoldResponseDto>builder()
                    .code(SuccessCode.OK.getCode()) //API 통신 자체는 성공
                    .message(FailureCode.WALLET_CHARGE_FAILED.getMessage()) // 잔액 부족 코드
                    .data(pgResponse)
                    .build();
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

        return CashApiResponse.<PayAndHoldResponseDto>builder()
                .code(SuccessCode.OK.getCode())
                .message("요청 성공 (Fake)")
                .data(paidResponse)
                .build();
    }
}
