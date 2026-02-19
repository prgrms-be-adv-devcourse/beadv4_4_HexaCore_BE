package com.back.detector.app;

import com.back.detector.exception.BidSpamException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectorFacadeTest {

    @Mock
    private BidSpamDetector bidSpamDetector;

    @InjectMocks
    private DetectorFacade detectorFacade;

    private static final Long USER_ID = 55L;

    @AfterEach
    void clearSecurityContext() {
    }

    private void setUpAuthenticatedContext() {
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
        @DisplayName("스팸 아닌 경우 checkBidSpam이 호출되어야 한다")
        void should_proceed_and_return_original_value() throws Throwable {
            detectorFacade.detectBidSpam(USER_ID);

            verify(bidSpamDetector).checkBidSpam(USER_ID);
        }

        @Test
        @DisplayName("정상 흐름에서 예외 없이 완료되어야 한다")
        void should_return_null_when_original_method_is_void() throws Throwable {
            detectorFacade.detectBidSpam(USER_ID);

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

            assertThatThrownBy(() -> detectorFacade.detectBidSpam(USER_ID))
                    .isInstanceOf(BidSpamException.class);
        }
    }

    // --- 인증 컨텍스트 없는 경우 ---

    @Nested
    @DisplayName("SecurityContext에 인증 정보가 없는 경우")
    class WhenAuthenticationMissing {

        @Test
        @DisplayName("userId가 null이면 checkBidSpam에 null이 전달되어야 한다")
        void should_pass_null_user_id() throws Throwable {
            detectorFacade.detectBidSpam(null);

            verify(bidSpamDetector).checkBidSpam(null);
        }
    }
}
