package com.back.notification.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.notification.app.pricealert.PriceAlertFacade;
import com.back.notification.dto.request.PriceAlertSaveRequestDto;
import com.back.notification.dto.response.PriceAlertIdDto;
import com.back.notification.dto.response.PriceAlertResponseDto;
import com.back.security.principal.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/price-alerts")
@RequiredArgsConstructor
public class PriceAlertController {
    private final PriceAlertFacade priceAlertFacade;

    @PostMapping
    public CommonResponse<PriceAlertIdDto> savePriceAlert(@RequestBody PriceAlertSaveRequestDto dto,
                                                          @AuthenticationPrincipal AuthPrincipal principal) {
        PriceAlertIdDto response = priceAlertFacade.save(dto, principal.getUserId());
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @GetMapping
    public CommonResponse<List<PriceAlertResponseDto>> getPriceAlerts(@AuthenticationPrincipal AuthPrincipal principal) {
        List<PriceAlertResponseDto> response = priceAlertFacade.findByUserId(principal.getUserId());
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @DeleteMapping("/{priceAlertId}")
    public CommonResponse<Void> deletePriceAlert(@PathVariable Long priceAlertId,
                                                  @AuthenticationPrincipal AuthPrincipal principal) {
        priceAlertFacade.delete(priceAlertId, principal.getUserId());
        return CommonResponse.success(SuccessCode.OK, null);
    }
}
