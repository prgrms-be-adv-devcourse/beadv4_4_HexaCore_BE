package com.back.market.adapter.out.detector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "detector-client", url = "${feign.detector.url}")
public interface DetectorFeignApi {

    @PostMapping("/api/v1/internal/detects/bid-spam/{userId}")
    void detectBidSpam(@PathVariable Long userId);

    @PostMapping("/api/v1/internal/detects/hijack")
    void detectHijack(@RequestBody DetectHijackRequestDto requestDto);
}
