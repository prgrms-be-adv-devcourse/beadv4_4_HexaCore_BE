package com.back.user.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.response.UserIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "사용자 관련 API")
public interface UserApiV1 {

    @Operation(
            summary = "FCM 토큰 등록/변경",
            description = """
                로그인한 사용자의 FCM 토큰을 등록하거나 변경합니다.
                FCM 토큰이 없던 사용자에게는 새로 등록하며,
                기존 토큰이 있는 경우에는 새로운 값으로 업데이트합니다.
                """
    )
    @ApiResponse(responseCode = "200", description = "토큰 등록/변경 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<UserIdResponse> registerOrUpdateFcmToken(
            AuthPrincipal authPrincipal,
            UpdateFcmTokenRequest request
    );

}
