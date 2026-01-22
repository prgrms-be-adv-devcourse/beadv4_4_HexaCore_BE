package com.back.settlement.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.common.response.CommonResponse;
import com.back.settlement.adapter.out.feign.cash.TestCashClient;
import com.back.settlement.adapter.out.feign.market.TestOrderClient;
import com.back.settlement.app.event.SettlementPayoutRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/feign")
@RequiredArgsConstructor
@Profile({"default", "local"})
@Tag(name = "Feign Test", description = "FeignClient 테스트용 API (개발 환경 전용)")
public class FeignTestController {
    private final TestOrderClient testOrderClient;
    private final TestCashClient testCashClient;

    @Operation(summary = "테스트 주문 데이터 추가", description = "TestOrderClient에 테스트용 주문 데이터를 추가합니다.")
    @PostMapping("/orders")
    public CommonResponse<Void> addTestOrder(@RequestBody SettlementTargetOrder order) {
        testOrderClient.addOrder(order);
        return CommonResponse.success(SuccessCode.CREATED, null);
    }

    @Operation(summary = "테스트 주문 데이터 초기화", description = "TestOrderClient의 모든 테스트 데이터를 삭제합니다.")
    @DeleteMapping("/orders")
    public CommonResponse<Void> clearOrders() {
        testOrderClient.clear();
        return CommonResponse.success(SuccessCode.OK, null);
    }

    @Operation(summary = "캐시 지급 내역 조회", description = "TestCashClient를 통해 요청된 지급 내역을 조회합니다.")
    @GetMapping("/cash/payout-history")
    public CommonResponse<List<SettlementPayoutRequest>> getPayoutHistory() {
        return CommonResponse.success(SuccessCode.OK, testCashClient.getPayoutHistory());
    }

    @Operation(summary = "캐시 지급 내역 초기화", description = "TestCashClient의 지급 히스토리를 초기화합니다.")
    @DeleteMapping("/cash/payout-history")
    public CommonResponse<Void> clearPayoutHistory() {
        testCashClient.clear();
        return CommonResponse.success(SuccessCode.OK, null);
    }
}
