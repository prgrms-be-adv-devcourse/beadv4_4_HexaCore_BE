package com.back.market;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.market.adapter.out.detector.DetectorClient;
import com.back.market.adapter.out.detector.MarketDetectorAdapter;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.nio.charset.Charset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class MarketDetectorAdapterTooManyRequestsTests {

    private static final Logger log = LoggerFactory.getLogger(MarketDetectorAdapterTooManyRequestsTests.class);

    @Autowired
    private MarketDetectorAdapter marketDetectorAdapter;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public DetectorClient detectorClient() {
            return new DetectorClient() {
                @Override
                public void detectBidSpam(Long userId) {
                    // 만들어낼 FeignException(429)
                    Response response = Response.builder()
                            .status(429)
                            .reason("Too Many Requests")
                            .request(Request.create(Request.HttpMethod.POST, "/api/v1/internal/detects/bid-spam/" + userId, Collections.emptyMap(), null, Charset.defaultCharset(), null))
                            .build();
                    throw FeignException.errorStatus("detectBidSpam", response);
                }

                @Override
                public void detectHijack(com.back.market.adapter.out.detector.dto.request.DetectHijackRequestDto requestDto) {
                    // no-op
                }
            };
        }
    }

    @Test
    @DisplayName("Detector(입찰탐지)가 429(Too Many Requests)를 반환하면 입찰 차단 예외(BID_SPAM_DETECTED)를 던진다")
    void detectBidSpam_429_throwsCustomException() {
        CustomException ex = assertThrows(CustomException.class, () -> marketDetectorAdapter.detectBidSpam(123L));
        // 로그로 예외 상태 출력
        log.info("Test caught CustomException - failureCode={}, message={}", ex.getFailureCode(), ex.getMessage(), ex);
        // 내부 FailureCode 검증
        assert ex.getFailureCode() == FailureCode.BID_SPAM_DETECTED;
    }
}
