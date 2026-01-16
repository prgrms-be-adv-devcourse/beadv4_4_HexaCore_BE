package com.back.user.app.auth;

import com.back.common.code.FailureCode;
import com.back.common.exception.UnauthorizedException;
import com.back.security.jwt.JWTUtil;
import com.back.user.adapter.out.RefreshStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class ReissueTokenUseCase {

    private final JWTUtil jwtUtil;
    private final RefreshStore refreshStore;

    @Value("${app.jwt.access-ttl}")
    private Duration accessTtl;

    @Value("${app.jwt.refresh-ttl}")
    private Duration refreshTtl;

    public String reissueAccessToken(String refreshToken, HttpServletResponse response) {

        // refresh 토큰이 없을 경우 예외 발생
        if (refreshToken == null) {
            throw new UnauthorizedException(FailureCode.TOKEN_MISSING);
        }

        try {

            String category = jwtUtil.getCategory(refreshToken);

            // refresh 토큰이 아닌 경우 예외 발생
            if (!"refresh".equals(category)) {
                throw new UnauthorizedException(FailureCode.TOKEN_CATEGORY_INVALID);
            }

            // refresh 토큰에서 userId, role값 획득
            Long userId = jwtUtil.getUserId(refreshToken);
            String role = jwtUtil.getRole(refreshToken);

            // Redis에 저장된 현재 refresh와 동일한지 확인
            if (!refreshStore.isValid(userId, refreshToken)) {
                throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
            }

            // 엑세스 토큰 및 refresh 토큰 생성
            String newAccess = jwtUtil.createJwt("access", userId, role, accessTtl.toMillis());
            String newRefresh = jwtUtil.createJwt("refresh", userId , role, refreshTtl.toMillis());

            //redis에 refresh 저장
            refreshStore.rotate(userId, newRefresh, refreshTtl);

            // 응답 쿠키에 refresh 토큰 추가
            response.addCookie(createCookie("refresh", newRefresh, refreshTtl));

            return newAccess;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new UnauthorizedException(FailureCode.TOKEN_EXPIRED);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            // 서명 불일치, 토큰 형식 오류, 파싱 오류
            throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
        }
    }

    private Cookie createCookie(String key, String value, Duration ttl) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge((int) ttl.getSeconds());
        cookie.setAttribute("SameSite", "Lax");
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

}

