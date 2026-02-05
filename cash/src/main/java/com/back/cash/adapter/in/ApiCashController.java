package com.back.cash.adapter.in;

import com.back.cash.app.usecase.GetWalletBalanceUseCase;
import com.back.cash.dto.response.WalletBalanceResponseDto;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class ApiCashController implements CashApiV1 {

    private final GetWalletBalanceUseCase getWalletBalanceUseCase;

    @Override
    @GetMapping("/me/wallet/balance")
    public CommonResponse<WalletBalanceResponseDto> getWalletBalance(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        WalletBalanceResponseDto response = getWalletBalanceUseCase.getWalletBalance(authPrincipal.getUserId());
        return CommonResponse.success(SuccessCode.OK, response);
    }

}
