package com.back.settlement.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import com.back.settlement.app.dto.request.SettlementItemSearchRequest;
import com.back.settlement.app.dto.request.SettlementSearchRequest;
import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.facade.SettlementFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementControllerV1 implements SettlementApiV1 {
    private final SettlementFacade settlementFacade;

    @GetMapping
    public CommonResponse<Page<SettlementResponse>> getSettlementsByDateRange(@AuthenticationPrincipal AuthPrincipal authPrincipal, @ModelAttribute SettlementSearchRequest request, Pageable pageable) {
        Page<SettlementResponse> response = settlementFacade.getSettlementsBySellerIdAndDateRange(
                authPrincipal.getUserId(),
                request.startDate(),
                request.endDate(),
                pageable
        );
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @GetMapping("/items")
    public CommonResponse<Page<SettlementItemResponse>> getSettlementItems(@AuthenticationPrincipal AuthPrincipal principal, @ModelAttribute SettlementItemSearchRequest request, Pageable pageable) {
        Page<SettlementItemResponse> response = settlementFacade.getSettlementItemsBySellerIdAndFilters(
                principal.getUserId(),
                request.orderId(),
                request.productId(),
                request.status(),
                request.startDate(),
                request.endDate(),
                pageable
        );
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @GetMapping("/items/{settlementItemId}")
    public CommonResponse<SettlementItemResponse> getSettlementItem(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable("settlementItemId") Long settlementItemId) {
        SettlementItemResponse response = settlementFacade.getSettlementItem(settlementItemId, principal.getUserId());
        return CommonResponse.success(SuccessCode.OK, response);
    }
}
