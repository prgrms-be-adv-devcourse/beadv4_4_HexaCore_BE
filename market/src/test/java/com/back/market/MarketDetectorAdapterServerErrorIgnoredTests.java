package com.back.market;

import com.back.market.adapter.out.detector.DetectorClient;
import com.back.market.adapter.out.detector.MarketDetectorAdapter;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.nio.charset.Charset;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MarketDetectorAdapterServerErrorIgnoredTests {

    private static final Logger log = LoggerFactory.getLogger(MarketDetectorAdapterServerErrorIgnoredTests.class);

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
                    Response response = Response.builder()
                            .status(500)
                            .reason("Internal Server Error")
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
    @DisplayName("Detector 서비스에서 500 에러가 발생하면 MarketDetectorAdapter는 예외를 흡수하고 정상 리턴한다 (서비스 장애 허용)")
    void detectBidSpam_500_isIgnored() {
        // 호출 시 예외를 흡수하고 종료되어야 함 (탐지 장애 시 입찰 허용)
        Assertions.assertDoesNotThrow(() -> marketDetectorAdapter.detectBidSpam(456L));
        log.info("Test: Detector 500 error ignored as expected for userId={}", 456L);
        assertThat(true).isTrue();
    }
}
