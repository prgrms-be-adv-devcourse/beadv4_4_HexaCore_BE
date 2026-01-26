package com.back.user.adapter.in.auth;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.user.app.auth.TestLoginUseCase;
import com.back.user.dto.request.TestLoginRequestDto;
import com.back.user.dto.response.TestLoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    @PostMapping("/test-login")
    public CommonResponse<TestLoginResponseDto> testLogin(@RequestBody TestLoginRequestDto req) {
        TestLoginUseCase.Result r = testLoginUseCase.execute(req.email());

        HttpHeaders headers = new HttpHeaders();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh", r.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(r.refreshTtl())
                .build();

        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return CommonResponse.success(SuccessCode.OK, new TestLoginResponseDto(r.accessToken()));
    }

}
