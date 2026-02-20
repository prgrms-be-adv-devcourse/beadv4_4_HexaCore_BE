package com.back.cash.adapter.in;

import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.request.TossFailRequestDto;
import com.back.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Cash", description = "캐시 관련 API")
public interface CashPaymentApiV1 {


    @Operation(
            summary = "토스 결제 승인 요청",
            description = """
                토스 결제시, 결제 검증을 위해서 paymentKey, tossOrderId, totalAmount를 받아서 검증합니다.
                """
    )
    @ApiResponse(responseCode = "200", description = "결제 승인 완료")
    @ApiResponse(responseCode = "202", description = "결제 상태 불확실 (Unknown) - 추후 재확인 필요", content = @Content)
    @ApiResponse(responseCode = "400", description = "금액 불일치 또는 잘못된 요청", content = @Content)
    @ApiResponse(responseCode = "422", description = "결제 거절 (토스 측 거절)", content = @Content)
    @ApiResponse(responseCode = "404", description = "결제 정보 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    ResponseEntity<CommonResponse<?>> confirm(@RequestBody TossConfirmRequest req);

    @Operation(
            summary = "토스 결제 실패 처리",
            description = "토스 결제 과정에서 문제가 발생했을 때 호출되어 선점된 홀딩 금액을 해제하고 실패 로그를 남깁니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실패 처리 완료"),
            @ApiResponse(responseCode = "404", description = "결제 정보 없음", content = @Content)
    })
    ResponseEntity<CommonResponse<?>> fail(@RequestBody TossFailRequestDto req);
}
