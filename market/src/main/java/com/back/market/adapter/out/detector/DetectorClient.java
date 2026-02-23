package com.back.market.adapter.out.detector;

import com.back.market.adapter.out.detector.dto.request.DetectHijackRequestDto;

public interface DetectorClient {
    void detectBidSpam(Long userId);
    void detectHijack(DetectHijackRequestDto requestDto);
}
