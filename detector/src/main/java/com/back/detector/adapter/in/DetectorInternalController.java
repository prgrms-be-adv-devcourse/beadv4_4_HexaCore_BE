package com.back.detector.adapter.in;

import com.back.detector.app.DetectorFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인터널 - Detector", description = "서비스 간 내부 호출용 Detector API")
@RestController
@RequestMapping("/api/v1/internal/detects")
@RequiredArgsConstructor
public class DetectorInternalController {

    private final DetectorFacade detectorFacade;

    @Operation(summary = "입찰 스팸 감지", description = "userId를 받아 입찰 스팸 여부를 감지한다. 스팸으로 판단되면 예외를 반환한다.")
    @PostMapping("/bid-spam/{userId}")
    public void detectBidSpam(@PathVariable Long userId) {
        detectorFacade.detectBidSpam(userId);
    }
}
