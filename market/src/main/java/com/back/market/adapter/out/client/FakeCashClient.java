package com.back.market.adapter.out.client;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import com.back.market.app.port.out.CashClient;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.response.CashApiResponse;
import com.back.market.dto.response.CashHoldResponseDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FakeCashClient implements CashClient {
    @Override
    public CashApiResponse<CashHoldResponseDto> requestBidHold(PayAndHoldRequestDto requestDto) {
        Long userId = requestDto.buyerId();
        BigDecimal amount = requestDto.totalAmount();

        // 잘 들어왔는지 확인용 로그
        System.out.println("[FakeCashClient] 요청 수신: " + requestDto);

        //테스트용(9999원 요청시 잔액 부족 에러가 나도록)
        if (amount.intValue() == 9999) {
            return CashApiResponse.<CashHoldResponseDto>builder()
                    .code(FailureCode.WALLET_CHARGE_FAILED.getCode()) // 잔액 부족 코드
                    .message(FailureCode.WALLET_CHARGE_FAILED.getMessage())
                    .data(null)
                    .build();
        }

        CashHoldResponseDto fakeData = CashHoldResponseDto.of(userId, amount);

        System.out.println("[FakeCashClient] User " + userId + "의 " + amount + "원 홀딩 성공 (Ref: " + requestDto.relType() + " / " + requestDto.relId() + ")");

        return CashApiResponse.<CashHoldResponseDto>builder()
                .code(SuccessCode.OK.getCode())
                .message("요청 성공 (Fake)")
                .data(fakeData)
                .build();
    }
}
