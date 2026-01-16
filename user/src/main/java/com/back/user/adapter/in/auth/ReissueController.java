package com.back.user.adapter.in.auth;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import com.back.common.exception.UnauthorizedException;
import com.back.common.response.CommonResponse;
import com.back.user.app.auth.ReissueTokenUseCase;
import com.back.user.dto.response.ReissueResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReissueController {

    private final ReissueTokenUseCase reissueTokenUseCase;

    @PostMapping("/reissue")
    public CommonResponse<ReissueResponse> reissue(
            @CookieValue(value = "refresh", required = false) String refresh,
            HttpServletResponse response
    ) {
        if (refresh == null || refresh.isBlank()) {
            throw new UnauthorizedException(FailureCode.TOKEN_MISSING);
        }

        String newAccess = reissueTokenUseCase.reissueAccessToken(refresh, response);
        return CommonResponse.success(
                SuccessCode.OK,
                new ReissueResponse(newAccess)
        );
    }
}

