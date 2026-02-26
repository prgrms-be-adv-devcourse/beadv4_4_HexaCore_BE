package com.back.detector.adapter.in;

import com.back.detector.app.DetectorFacade;
import com.back.detector.dto.request.DetectHijackRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


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

    @Operation(summary = "계정 탈취 감지", description = "userId, userEmail, ip, transactionAmount를 받아 계정 탈취 여부를 감지한다.")
    @PostMapping("/hijack")
    public void detectHijack(@RequestBody DetectHijackRequestDto requestDto) {
        detectorFacade.detectHijack(requestDto.userId(), requestDto.userEmail(), requestDto.ip(), requestDto.transactionAmount());
    }
}
