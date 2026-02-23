package com.back.market.adapter.out.detector;

import com.back.market.adapter.out.detector.dto.request.DetectHijackRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("!local & !test")
@Component
@RequiredArgsConstructor
public class DetectorClientHttp implements DetectorClient {

    private final DetectorFeignApi detectorFeignApi;

    @Override
    public void detectBidSpam(Long userId) {
        detectorFeignApi.detectBidSpam(userId);
    }

    @Override
    public void detectHijack(DetectHijackRequestDto requestDto) {
        detectorFeignApi.detectHijack(requestDto);
    }
}
