package com.back.user.app.auth;

import com.back.common.code.FailureCode;
import com.back.common.exception.UnauthorizedException;
import com.back.security.jwt.JWTUtil;
import com.back.user.adapter.out.RefreshStore;
import com.back.user.dto.response.TokenResponseDto;
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

    public TokenResponseDto execute(String refreshToken) {
        requirePresent(refreshToken);

        try {

            if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
                throw new UnauthorizedException(FailureCode.TOKEN_CATEGORY_INVALID);
            }

            Long userId = jwtUtil.getUserId(refreshToken);
            String role = jwtUtil.getRole(refreshToken);

            if (!refreshStore.isValid(userId, refreshToken)) {
                throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
            }

            return reissue(userId, role);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new UnauthorizedException(FailureCode.TOKEN_EXPIRED);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            // 서명 불일치, 토큰 형식 오류, 파싱 오류
            throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
        }
    }

    private void requirePresent(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException(FailureCode.TOKEN_MISSING);
        }
    }

    private TokenResponseDto reissue(Long userId, String role) {
        String newAccess = jwtUtil.createJwt("access", userId, role, accessTtl.toMillis());
        String newRefresh = jwtUtil.createJwt("refresh", userId, role, refreshTtl.toMillis());

        refreshStore.rotate(userId, newRefresh, refreshTtl);

        return new TokenResponseDto(newAccess, newRefresh, refreshTtl);
    }
}

