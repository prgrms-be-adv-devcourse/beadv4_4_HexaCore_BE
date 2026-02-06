package com.back.cash;

import com.back.cash.adapter.out.TossPaymentsClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TossPaymentsClientTest {

    private MockWebServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
    }

    @Test
    @DisplayName("읽기 타임아웃 초과 시 ResourceAccessException 발생")
    void confirm_whenReadTimeoutExceeded_thenThrowResourceAccessException() {
        // given: 서버가 연결은 수락하지만 응답을 보내지 않음 → 읽기 타임아웃 발생
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        TossPaymentsClient client = new TossPaymentsClient(
                "test-secret",
                mockServer.url("/").toString(),
                5000,   // connect timeout 5초
                1000);  // read timeout 1초

        // when & then
        assertThatThrownBy(() -> client.confirm("pk_test", "order_123", new BigDecimal("10000")))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    @DisplayName("타임아웃 내 정상 응답 시 예외 없이 완료")
    void confirm_whenResponseWithinTimeout_thenSuccess() {
        // given: 즉시 200 응답
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        TossPaymentsClient client = new TossPaymentsClient(
                "test-secret",
                mockServer.url("/").toString(),
                5000,
                5000);

        // when & then
        assertThatCode(() -> client.confirm("pk_test", "order_123", new BigDecimal("10000")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("confirm 요청이 올바른 경로와 헤더로 전송된다")
    void confirm_sendsCorrectRequestPathAndHeaders() throws Exception {
        // given
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        TossPaymentsClient client = new TossPaymentsClient(
                "test-secret",
                mockServer.url("/").toString(),
                5000,
                5000);

        // when
        client.confirm("pk_test", "order_123", new BigDecimal("10000"));

        // then
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v1/payments/confirm");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Authorization")).startsWith("Basic ");
        assertThat(request.getHeader("Content-Type")).contains("application/json");
    }
}
