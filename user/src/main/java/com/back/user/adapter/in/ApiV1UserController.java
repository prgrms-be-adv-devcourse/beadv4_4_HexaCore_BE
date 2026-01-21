package com.back.user.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import com.back.user.app.UserFacade;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.response.UserIdResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
