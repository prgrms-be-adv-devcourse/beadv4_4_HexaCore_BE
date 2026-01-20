package com.back.market.app.port.out;

import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.response.CashApiResponse;
import com.back.market.dto.response.PayAndHoldResponseDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface CashClient {

    /**
     * Cash 모듈에 Hold를 요청(구매 입찰 등록 시)
     * @param requestDto 요청 dto
     * @return 성공 여부(true: 성공, false: 잔액부족 등 실패)
     */
    CashApiResponse<PayAndHoldResponseDto> requestBidHold(@RequestBody PayAndHoldRequestDto requestDto);
}
