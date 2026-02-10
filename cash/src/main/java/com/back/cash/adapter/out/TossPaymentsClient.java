package com.back.cash.adapter.out;

import com.back.cash.adapter.out.exception.TossPaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Component
public class TossPaymentsClient {

    private final RestClient restClient;
    private final String secretKey;
    private final JsonMapper jsonMapper;

    public TossPaymentsClient(
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.base-url:https://api.tosspayments.com}") String baseUrl,
            @Value("${toss.connect-timeout:5000}") int connectTimeout,
            @Value("${toss.read-timeout:30000}") int readTimeout,
            JsonMapper jsonMapper) {
        this.secretKey = secretKey;
        this.jsonMapper = jsonMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public void confirm(String paymentKey, String orderId, BigDecimal amount) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        try {
            restClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "paymentKey", paymentKey,
                            "orderId", orderId,
                            "amount", amount
                    ))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            TossErrorDto errorDto = parseError(errorBody);
            throw new TossPaymentException(errorDto.code(), errorDto.message());
        }
    }

    private TossErrorDto parseError(String jsonBody) {
        try {
            return jsonMapper.readValue(jsonBody, TossErrorDto.class);
        } catch (Exception e) {
            return new TossErrorDto("UNKNOWN", "결제 승인 중 알 수 없는 오류가 발생했습니다.");
        }
    }

    record TossErrorDto(String code, String message) {}
}

