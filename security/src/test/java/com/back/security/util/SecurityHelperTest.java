package com.back.security.util;

import com.back.security.principal.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityHelperTest {

    private static final Long USER_ID = 77L;
    private static final String ROLE = "USER";

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // --- 정상 흐름 ---

    @Nested
    @DisplayName("유효한 인증 정보가 있는 경우")
    class WhenAuthenticated {

        private void setUpContext(Long userId, String role) {
            AuthPrincipal principal = new AuthPrincipal(userId, role);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            SecurityContextImpl ctx = new SecurityContextImpl();
            ctx.setAuthentication(authentication);

            SecurityContextHolder.setContext(ctx);
        }

        @Test
        @DisplayName("getCurrentUserId()는 올바른 유저 ID를 반환해야 한다")
        void should_return_correct_user_id() {
            setUpContext(USER_ID, ROLE);

            assertThat(SecurityHelper.getCurrentUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("getCurrentPrincipal()는 올바른 AuthPrincipal 객체를 반환해야 한다")
        void should_return_correct_principal() {
            setUpContext(USER_ID, ROLE);

            AuthPrincipal principal = SecurityHelper.getCurrentPrincipal();

            assertThat(principal.getUserId()).isEqualTo(USER_ID);
            assertThat(principal.getRole()).isEqualTo(ROLE);
        }

        @Test
        @DisplayName("다른 유저 ID로도 올바르게 반환해야 한다")
        void should_return_different_user_id() {
            Long anotherUserId = 123L;
            setUpContext(anotherUserId, "ROLE_ADMIN");

            assertThat(SecurityHelper.getCurrentUserId()).isEqualTo(anotherUserId);
        }
    }

    // --- 인증 없음 ---

    @Nested
    @DisplayName("인증 정보가 없거나 유효하지 않은 경우")
    class WhenAuthenticationMissing {

        @Test
        @DisplayName("Authentication이 null이면 getCurrentUserId()에서 IllegalStateException 발생")
        void should_throw_when_authentication_is_null() {
            // Authentication이 null인 상태 → SecurityContextImpl 기본값
            SecurityContextHolder.setContext(new SecurityContextImpl());

            assertThatThrownBy(SecurityHelper::getCurrentUserId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 정보가 없습니다");
        }

        @Test
        @DisplayName("isAuthenticated=false이면 getCurrentUserId()에서 IllegalStateException 발생")
        void should_throw_when_not_authenticated() {
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            SecurityContextImpl ctx = new SecurityContextImpl();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);

            assertThatThrownBy(SecurityHelper::getCurrentUserId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 정보가 없습니다");
        }

        @Test
        @DisplayName("Principal이 AuthPrincipal이 아닌 타입이면 IllegalStateException 발생")
        void should_throw_when_principal_is_wrong_type() {
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn("not-an-AuthPrincipal"); // String 타입

            SecurityContextImpl ctx = new SecurityContextImpl();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);

            assertThatThrownBy(SecurityHelper::getCurrentUserId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 정보가 없습니다");
        }

        @Test
        @DisplayName("Authentication이 null이면 getCurrentPrincipal()에서도 IllegalStateException 발생")
        void should_throw_on_getCurrentPrincipal_when_null() {
            SecurityContextHolder.setContext(new SecurityContextImpl());

            assertThatThrownBy(SecurityHelper::getCurrentPrincipal)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 정보가 없습니다");
        }
    }
}
