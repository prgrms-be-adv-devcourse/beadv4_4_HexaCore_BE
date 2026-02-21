package com.back.detector.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.detector.app.DetectorFacade;
import com.back.detector.dto.response.BidSpamLogResponse;
import com.back.detector.dto.response.CrawlingLogResponse;
import com.back.detector.dto.response.HijackLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 - 탐지 로그", description = "보안 탐지 로그 조회 API (ADMIN 전용)")
@Validated
@RestController
@RequestMapping("/api/v1/admin/detects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DetectorAdminController {

    private final DetectorFacade detectorFacade;

    @Operation(summary = "입찰 스팸 로그 조회", description = "입찰 스팸으로 탐지된 로그 목록을 페이지 단위로 조회한다.")
    @GetMapping("/bid-spam-logs")
    public CommonResponse<Page<BidSpamLogResponse>> getBidSpamLogs(
            @RequestParam(defaultValue = "0") int page,
            @Max(1000) @RequestParam(defaultValue = "20") int size
    ) {
        return CommonResponse.success(SuccessCode.OK, detectorFacade.findLatestBidSpamLogs(page, size));
    }

    @Operation(summary = "크롤링 탐지 로그 조회", description = "크롤링 봇으로 탐지된 로그 목록을 페이지 단위로 조회한다.")
    @GetMapping("/crawling-logs")
    public CommonResponse<Page<CrawlingLogResponse>> getCrawlingLogs(
            @RequestParam(defaultValue = "0") int page,
            @Max(1000) @RequestParam(defaultValue = "20") int size
    ) {
        return CommonResponse.success(SuccessCode.OK, detectorFacade.findLatestCrawlingLogs(page, size));
    }

    @Operation(summary = "세션 하이재킹 탐지 로그 조회", description = "세션 하이재킹으로 탐지된 로그 목록을 페이지 단위로 조회한다.")
    @GetMapping("/hijack-logs")
    public CommonResponse<Page<HijackLogResponse>> getHijackLogs(
            @RequestParam(defaultValue = "0") int page,
            @Max(1000) @RequestParam(defaultValue = "20") int size
    ) {
        return CommonResponse.success(SuccessCode.OK, detectorFacade.findLatestHijackLogs(page, size));
    }
}
