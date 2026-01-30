package com.back.user.adapter.in.auth;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.user.app.auth.RefreshCookieSupport;
import com.back.user.app.auth.TestLoginUseCase;
import com.back.user.dto.request.TestLoginRequestDto;
import com.back.user.dto.response.TestLoginResponseDto;
import com.back.user.dto.response.TokenResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile("local")
@RequestMapping("/api/v1/auth")
public class TestLoginController {
    private final TestLoginUseCase testLoginUseCase;
    private final RefreshCookieSupport refreshCookieSupport;

    @PostMapping("/test-login")
    public CommonResponse<TestLoginResponseDto> testLogin(@RequestBody TestLoginRequestDto req, HttpServletResponse response) {
        TokenResponseDto tokenResponseDto = testLoginUseCase.execute(req.email());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieSupport.createRefreshSetCookieHeader(tokenResponseDto.refreshToken(), tokenResponseDto.refreshTtl()));

        return CommonResponse.success(SuccessCode.OK, new TestLoginResponseDto(tokenResponseDto.accessToken()));
    }

}
