package com.back.detector.app;

import com.back.detector.exception.BidSpamException;
import com.back.security.principal.AuthPrincipal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectorFacadeTest {

    @Mock
    private BidSpamDetector bidSpamDetector;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private DetectorFacade detectorFacade;

    private static final Long USER_ID = 55L;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /** SecurityContext에 유효한 인증 정보를 세팅하는 헬퍼 */
    private void setUpAuthenticatedContext() {
        AuthPrincipal principal = new AuthPrincipal(USER_ID, "ROLE_USER");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    // --- 정상 흐름 ---

    @Nested
    @DisplayName("스팸 아닌 정상 입찰 흐름")
    class WhenSpamCheckPasses {

        @BeforeEach
        void setUp() {
            setUpAuthenticatedContext();
        }

        @Test
        @DisplayName("스팸 아닌 경우 원래 메서드가 정상 실행되고 반환값을 그대로 전달해야 한다")
        void should_proceed_and_return_original_value() throws Throwable {
            String expectedReturn = "bid-success";
            when(joinPoint.proceed()).thenReturn(expectedReturn);

            Object result = detectorFacade.detectBidSpam(joinPoint);

            assertThat(result).isEqualTo(expectedReturn);
            verify(bidSpamDetector).checkBidSpam(USER_ID);
            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("원래 메서드가 void(null 반환)이면 null을 그대로 반환해야 한다")
        void should_return_null_when_original_method_is_void() throws Throwable {
            when(joinPoint.proceed()).thenReturn(null);

            Object result = detectorFacade.detectBidSpam(joinPoint);

            assertThat(result).isNull();
            verify(bidSpamDetector).checkBidSpam(USER_ID);
        }
    }

    // --- 스팸 감지 시 예외 전파 ---

    @Nested
    @DisplayName("BidSpamException 발생 시 동작")
    class WhenSpamDetected {

        @BeforeEach
        void setUp() {
            setUpAuthenticatedContext();
        }

        @Test
        @DisplayName("BidSpamException이 발생하면 그대로 상위로 전파되어야 한다")
        void should_rethrow_bid_spam_exception() {
            doThrow(new BidSpamException()).when(bidSpamDetector).checkBidSpam(USER_ID);

            assertThatThrownBy(() -> detectorFacade.detectBidSpam(joinPoint))
                    .isInstanceOf(BidSpamException.class);

            // 스팸이 감지되면 원래 메서드는 실행되지 않아야 한다
            verifyNoInteractions(joinPoint);
        }
    }

    // --- 원래 메서드에서 예외 발생 시 --

    @Nested
    @DisplayName("원래 메서드(joinPoint.proceed)에서 예외 발생 시 동작")
    class WhenOriginalMethodThrows {

        @BeforeEach
        void setUp() { setUpAuthenticatedContext(); }

        @Test
        @DisplayName("원래 메서드가 체크드 예외를 던지면 RuntimeException으로 래핑되고, cause에 원본 예외가 포함되어야 한다")
        void should_wrap_checked_exception_in_runtime_exception() throws Throwable {
            Exception originalCause = new Exception("DB 연결 실패");
            when(joinPoint.proceed()).thenThrow(originalCause);

            assertThatThrownBy(() -> detectorFacade.detectBidSpam(joinPoint))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("입찰 처리 실패")
                    .hasCause(originalCause);   // cause가 원본 예외
        }

        @Test
        @DisplayName("원래 메서드가 RuntimeException을 던지면 래핑된 RuntimeException으로 전파되어야 한다")
        void should_wrap_runtime_exception_as_well() throws Throwable {
            RuntimeException originalCause = new RuntimeException("일반 오류");
            when(joinPoint.proceed()).thenThrow(originalCause);

            assertThatThrownBy(() -> detectorFacade.detectBidSpam(joinPoint))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("입찰 처리 실패")
                    .hasCause(originalCause);
        }
    }

    // --- 인증 컨텍스트 없는 경우 ---

    @Nested
    @DisplayName("SecurityContext에 인증 정보가 없는 경우")
    class WhenAuthenticationMissing {

        @Test
        @DisplayName("인증 정보가 없으면 RuntimeException으로 래핑되고, cause가 IllegalStateException이어야 한다")
        void should_throw_when_no_authentication() {
            // SecurityContextHolder는 기본상태(빈 컨텍스트)로 유지

            assertThatThrownBy(() -> detectorFacade.detectBidSpam(joinPoint))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("입찰 처리 실패")
                    .cause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 정보가 없습니다");
        }

        @Test
        @DisplayName("인증되지 않은(isAuthenticated=false) 경우도 동일하게 래핑되어야 한다")
        void should_throw_when_not_authenticated() {
            Authentication unauthenticated = mock(Authentication.class);
            when(unauthenticated.isAuthenticated()).thenReturn(false);

            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(unauthenticated);
            SecurityContextHolder.setContext(context);

            assertThatThrownBy(() -> detectorFacade.detectBidSpam(joinPoint))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("입찰 처리 실패")
                    .cause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 정보가 없습니다");
        }
    }
}
