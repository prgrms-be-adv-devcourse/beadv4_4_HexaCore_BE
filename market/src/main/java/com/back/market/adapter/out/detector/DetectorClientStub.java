package com.back.market.adapter.out.detector;

import com.back.market.adapter.out.detector.dto.request.DetectHijackRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Detector 모듈의 기능을 하는 가짜 클라이언트
 */
@Slf4j
@Component
@Profile({"local", "test"})
public class DetectorClientStub implements DetectorClient {

    @Override
    public void detectBidSpam(Long userId) {
        log.info("[FakeDetectorClient] 입찰 스팸 감지 요청 수신: userId={}", userId);
    }

    @Override
    public void detectHijack(DetectHijackRequestDto requestDto) {
        log.info("[FakeDetectorClient] 계정 탈취 감지 요청 수신: userId={}", requestDto.userId());
    }
}
