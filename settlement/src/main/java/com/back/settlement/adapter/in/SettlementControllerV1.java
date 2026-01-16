package com.back.settlement.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.facade.SettlementFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementControllerV1 implements SettlementApiV1 {
    private final SettlementFacade settlementFacade;

    @GetMapping
    public CommonResponse<List<SettlementResponse>> getSettlements() {
        // TODO: 권한 체크 필요 (인증된 사용자에서 sellerId 추출)
        Long sellerId = 1L;
        List<SettlementResponse> response = settlementFacade.getSettlements(sellerId);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @GetMapping("/{settlementItemId}")
    public CommonResponse<SettlementItemResponse> getSettlementItem(@PathVariable("settlementItemId") Long settlementItemId) {
        // TODO: 권한 체크 필요 (인증된 사용자에서 sellerId 추출)
        Long sellerId = 1L;
        SettlementItemResponse response = settlementFacade.getSettlementItem(settlementItemId, sellerId);
        return CommonResponse.success(SuccessCode.OK, response);
    }
}
