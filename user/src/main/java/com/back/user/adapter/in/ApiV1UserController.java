package com.back.user.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import com.back.user.app.UserFacade;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.request.UpdateNotificationSettingsRequest;
import com.back.user.dto.request.UpdateUserProfileRequestDto;
import com.back.user.dto.response.NotificationSettingResponse;
import com.back.user.dto.response.UpdateUserProfileResponseDto;
import com.back.user.dto.response.UserIdResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ApiV1UserController implements UserApiV1 {
    private final UserFacade userFacade;

    @Override
    @PatchMapping("/me/fcm-token")
    public CommonResponse<UserIdResponse> registerOrUpdateFcmToken(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                      @Valid @RequestBody UpdateFcmTokenRequest request) {
        UserIdResponse response = userFacade.registerOrUpdateFcmToken(authPrincipal.getUserId(), request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @GetMapping("/me/notifications/setting")
    public CommonResponse<NotificationSettingResponse> getNotificationSettings(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        NotificationSettingResponse response = userFacade.getNotificationSettings(authPrincipal.getUserId());
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @PatchMapping("/me/notifications/setting")
    public CommonResponse<UserIdResponse> updateNotificationSettings(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                                     @Valid @RequestBody UpdateNotificationSettingsRequest request) {
        UserIdResponse response = userFacade.updateNotificationSettings(authPrincipal.getUserId(), request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @PatchMapping("/me/profile")
    public CommonResponse<UpdateUserProfileResponseDto> updateUserProfile(@AuthenticationPrincipal AuthPrincipal authPrincipal, @Valid @RequestBody UpdateUserProfileRequestDto request) {
        UpdateUserProfileResponseDto response = userFacade.updateUserProfile(authPrincipal.getUserId(), request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @GetMapping("/me")
    public CommonResponse<>
}
