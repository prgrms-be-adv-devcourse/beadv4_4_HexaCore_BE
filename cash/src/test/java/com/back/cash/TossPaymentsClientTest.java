package com.back.cash;

import com.back.cash.adapter.out.TossPaymentsClient;
import com.back.cash.adapter.out.exception.TossPaymentException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TossPaymentsClientTest {

    private MockWebServer mockServer;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
    }

    private TossPaymentsClient createClient(int connectTimeout, int readTimeout) {
        return new TossPaymentsClient(
                "test-secret",
                mockServer.url("/").toString(),
                connectTimeout,
                readTimeout,
                jsonMapper);
    }

    @Test
    @DisplayName("읽기 타임아웃 초과 시 ResourceAccessException 발생")
    void confirm_whenReadTimeoutExceeded_thenThrowResourceAccessException() {
        // given
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        TossPaymentsClient client = createClient(5000, 1000);

        // when & then
        assertThatThrownBy(() -> client.confirm("pk_test", "order_123", new BigDecimal("10000")))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    @DisplayName("타임아웃 내 정상 응답 시 예외 없이 완료")
    void confirm_whenResponseWithinTimeout_thenSuccess() {
        // given
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        TossPaymentsClient client = createClient(5000, 5000);

        // when & then
        assertThatCode(() -> client.confirm("pk_test", "order_123", new BigDecimal("10000")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("confirm 요청이 올바른 경로와 헤더로 전송된다")
    void confirm_sendsCorrectRequestPathAndHeaders() throws Exception {
        // given
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        TossPaymentsClient client = createClient(5000, 5000);

        // when
        client.confirm("pk_test", "order_123", new BigDecimal("10000"));

        // then
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v1/payments/confirm");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Authorization")).startsWith("Basic ");
        assertThat(request.getHeader("Content-Type")).contains("application/json");
    }

    @Test
    @DisplayName("4xx 에러 시 TossPaymentException에 code/message가 파싱된다")
    void confirm_when4xxError_thenThrowTossPaymentExceptionWithParsedBody() {
        // given
        String errorBody = """
                {"code": "NOT_FOUND_PAYMENT", "message": "존재하지 않는 결제 입니다."}
                """;
        mockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody(errorBody));

        TossPaymentsClient client = createClient(5000, 5000);

        // when & then
        assertThatThrownBy(() -> client.confirm("pk_test", "order_123", new BigDecimal("10000")))
                .isInstanceOf(TossPaymentException.class)
                .satisfies(ex -> {
                    TossPaymentException tpe = (TossPaymentException) ex;
                    assertThat(tpe.getCode()).isEqualTo("NOT_FOUND_PAYMENT");
                    assertThat(tpe.getMessage()).isEqualTo("존재하지 않는 결제 입니다.");
                });
    }

    @Test
    @DisplayName("에러 응답 바디 파싱 실패 시 UNKNOWN 코드로 TossPaymentException 발생")
    void confirm_whenErrorBodyUnparseable_thenThrowTossPaymentExceptionWithUnknown() {
        // given
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "text/plain")
                .setBody("Internal Server Error"));

        TossPaymentsClient client = createClient(5000, 5000);

        // when & then
        assertThatThrownBy(() -> client.confirm("pk_test", "order_123", new BigDecimal("10000")))
                .isInstanceOf(TossPaymentException.class)
                .satisfies(ex -> {
                    TossPaymentException tpe = (TossPaymentException) ex;
                    assertThat(tpe.getCode()).isEqualTo("UNKNOWN");
                });
    }
}
