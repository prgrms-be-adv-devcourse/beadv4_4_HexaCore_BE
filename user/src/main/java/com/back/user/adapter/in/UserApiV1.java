package com.back.user.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.request.UpdateNotificationSettingsRequest;
import com.back.user.dto.response.NotificationSettingResponse;
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

    @Operation(
            summary = "알림 설정 조회",
            description = """
            로그인한 사용자의 알림 설정을 조회합니다.
            각 알림 항목의 ON/OFF 상태를 확인할 수 있습니다.
            """
    )
    @ApiResponse(responseCode = "200", description = "알림 설정 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "알림 설정 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<NotificationSettingResponse> getNotificationSettings(AuthPrincipal authPrincipal);

    @Operation(
            summary = "알림 설정 변경",
            description = """
            로그인한 사용자의 알림 설정을 변경합니다.
            항목별로 ON/OFF 설정이 가능하며,
            요청에 포함된 필드만 부분적으로 변경(PATCH)됩니다.
            """
    )
    @ApiResponse(responseCode = "200", description = "알림 설정 변경 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "알림 설정 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<UserIdResponse> updateNotificationSettings(AuthPrincipal authPrincipal,
                                                              UpdateNotificationSettingsRequest request);

}
