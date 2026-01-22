package com.back.market.adapter.out.cash;

import com.back.common.response.CommonResponse;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.request.PaymentCancelRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * MarketSupport(비즈니스 로직)가 바라보는 공통 인터페이스
 */
public interface CashClient {

    /**
     * Cash 모듈에 Hold를 요청(구매 입찰 등록 시)
     * @param requestDto 요청 dto
     * @return 성공 여부(true: 성공, false: 잔액부족 등 실패)
     */
    CommonResponse<PayAndHoldResponseDto> requestBidHold(@RequestBody PayAndHoldRequestDto requestDto);

    /**
     * Cash 모듈에 구매 입찰 취소 시 예치금 환불(홀딩 해제) 요청
     * @param requestDto 요청 dto
     * @return 성공 여부 및 결과 DTO
     */
    CommonResponse<PaymentCancelResponseDto> refundBidHold(@RequestBody PaymentCancelRequestDto requestDto);
}
