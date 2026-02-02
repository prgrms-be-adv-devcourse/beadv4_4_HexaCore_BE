package com.back.market.adapter.in;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.common.response.CommonResponse;
import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Market Internal API", description = "내부 통신용 API")
@RequestMapping("/api/v1/internal/market")
public interface ApiV1MarketInternal {

    @Operation(summary = "결제 완료 통지 수신 (Internal Callback)", description = "Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정한다.")
    @PostMapping("/payments/confirm")
    CommonResponse<Void> confirmPayment(@RequestBody PaymentCompletedRequestDto requestDto);

    // TODO 사용자가 결제 도중 실패했을 때 받아주는 컨트롤러 없음
    // /api/v1/internal/market/payments/fail

    @Operation(summary = "정산 모듈로 주문 데이터 전송")
    @GetMapping("/orders/settlement-target")
    List<SettlementTargetOrder> findSettlementTargetOrders(
            @RequestParam("targetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
