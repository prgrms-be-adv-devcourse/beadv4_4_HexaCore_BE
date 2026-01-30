package com.back.user.adapter.in.auth;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import com.back.user.app.auth.LogoutUseCase;
import com.back.user.app.auth.RefreshCookieSupport;
import com.back.user.app.auth.ReissueTokenUseCase;
import com.back.user.dto.response.ReissueResponse;
import com.back.user.dto.response.TokenResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class AuthController {

    private final ReissueTokenUseCase reissueTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshCookieSupport refreshCookieSupport;

    @PostMapping("/reissue")
    public CommonResponse<ReissueResponse> reissue(
            @CookieValue(value = "refresh", required = false) String refresh,
            HttpServletResponse response) {

        TokenResponseDto responseDto = reissueTokenUseCase.execute(refresh);

        response.addHeader(HttpHeaders.SET_COOKIE,
                refreshCookieSupport.createRefreshSetCookieHeader(responseDto.refreshToken(), responseDto.refreshTtl()));

        return CommonResponse.success(
                SuccessCode.OK,
                new ReissueResponse(responseDto.accessToken())
        );
    }

    @PostMapping("/logout")
    public CommonResponse<Void> logout(@AuthenticationPrincipal AuthPrincipal principal,
                                       HttpServletResponse response) {
        logoutUseCase.logout(principal.getUserId());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieSupport.deleteRefreshCookieHeader());

        return CommonResponse.success(SuccessCode.OK, null);
    }
}

