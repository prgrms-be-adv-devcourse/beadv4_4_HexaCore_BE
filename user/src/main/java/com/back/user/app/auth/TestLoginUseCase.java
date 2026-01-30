package com.back.user.app.auth;

import com.back.common.code.FailureCode;
import com.back.common.exception.EntityNotFoundException;
import com.back.security.jwt.JWTUtil;
import com.back.user.adapter.out.RefreshStore;
import com.back.user.adapter.out.UserRepository;
import com.back.user.domain.User;
import com.back.user.dto.response.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TestLoginUseCase {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RefreshStore refreshStore;

    @Value("${app.jwt.access-ttl}")
    private Duration accessTtl;

    @Value("${app.jwt.refresh-ttl}")
    private Duration refreshTtl;

    @Transactional(readOnly = true)
    public TokenResponseDto execute(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));

        String role = user.getRole().name();

        String access = jwtUtil.createJwt("access", user.getId(), role, accessTtl.toMillis());
        String refresh = jwtUtil.createJwt("refresh", user.getId(), role, refreshTtl.toMillis());

        refreshStore.save(user.getId(), refresh, refreshTtl);

        return new TokenResponseDto(access, refresh, refreshTtl);
    }
}
