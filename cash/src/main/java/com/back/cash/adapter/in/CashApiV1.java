package com.back.cash.adapter.in;

import com.back.cash.dto.response.WalletBalanceResponseDto;
import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cash", description = "캐시 관련 API")
public interface CashApiV1 {

    @Operation(
            summary = "내 지갑 잔액 조회",
            description = """
                로그인한 사용자의 지갑 잔액을 조회합니다.            
                """
    )
    @ApiResponse(responseCode = "200", description = "지갑 잔액 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "지갑 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<WalletBalanceResponseDto> getWalletBalance(AuthPrincipal authPrincipal);
}
