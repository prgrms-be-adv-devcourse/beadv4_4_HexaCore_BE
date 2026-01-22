package com.back.settlement.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.settlement.app.dto.response.SettlementLogResponse;
import com.back.settlement.app.facade.SettlementFacade;
import com.back.settlement.app.usecase.SettlementLogReadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "정산 로그 관련 API (Admin)", description = "정산 로그 조회 API (관리자 전용)")
@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
public class SettlementLogController {
    private final SettlementFacade settlementFacade;

    @Operation(summary = "정산 로그 조회", description = "특정 정산서의 상태 변경 로그를 조회합니다.")
    @GetMapping("/{settlementId}/logs")
    public CommonResponse<List<SettlementLogResponse>> getSettlementLogs() {
        // TODO: 권한 체크 필요 (인증된 사용자에서 sellerId 추출)
        Long sellerId = 1L;
        List<SettlementLogResponse> logs = settlementFacade.getLogsBySettlementId(sellerId);
        return CommonResponse.success(SuccessCode.OK, logs);
    }

    @Operation(summary = "전체 로그 조회", description = "모든 정산 상태 변경 로그를 조회합니다.")
    @GetMapping("/logs")
    public CommonResponse<List<SettlementLogResponse>> getAllLogs() {
        List<SettlementLogResponse> logs = settlementFacade.getAllLogs();
        return CommonResponse.success(SuccessCode.OK, logs);
    }
}
