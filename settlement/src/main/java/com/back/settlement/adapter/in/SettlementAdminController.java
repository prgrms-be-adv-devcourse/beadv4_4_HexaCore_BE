package com.back.settlement.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.settlement.app.dto.response.BatchExecutionResponse;
import com.back.settlement.app.dto.response.SettlementDashboardResponse;
import com.back.settlement.app.dto.response.SettlementLogResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.app.facade.SettlementFacade;
import com.back.settlement.batch.SettlementJobHistoryLauncher;
import com.back.settlement.batch.SettlementJobLauncher;
import com.back.settlement.domain.SettlementStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SettlementAdminController implements SettlementAdminApi {
    private final SettlementFacade settlementFacade;
    private final SettlementJobLauncher settlementJobLauncher;
    private final SettlementJobHistoryLauncher settlementJobHistoryLauncher;

    @GetMapping("/dashboard")
    public CommonResponse<SettlementDashboardResponse> getDashboard() {
        SettlementDashboardResponse dashboard = settlementFacade.getDashboard();
        return CommonResponse.success(SuccessCode.OK, dashboard);
    }

    @GetMapping
    public CommonResponse<Page<SettlementResponse>> getSettlements(
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<SettlementResponse> settlements = settlementFacade.getSettlementsFilter(status, sellerId, startDate, endDate, pageable);
        return CommonResponse.success(SuccessCode.OK, settlements);
    }

    @GetMapping("/{settlementId}")
    public CommonResponse<SettlementResponse> getSettlement(@PathVariable Long settlementId) {
        SettlementResponse settlement = settlementFacade.getSettlementById(settlementId);
        return CommonResponse.success(SuccessCode.OK, settlement);
    }

    @GetMapping("/{settlementId}/logs")
    public CommonResponse<List<SettlementLogResponse>> getSettlementLogs(@PathVariable Long settlementId) {
        List<SettlementLogResponse> logs = settlementFacade.getLogsBySettlementId(settlementId);
        return CommonResponse.success(SuccessCode.OK, logs);
    }

    @GetMapping("/logs")
    public CommonResponse<List<SettlementLogResponse>> getAllLogs() {
        List<SettlementLogResponse> logs = settlementFacade.getAllLogs();
        return CommonResponse.success(SuccessCode.OK, logs);
    }

    @Override
    @PostMapping("/batch/daily")
    public CommonResponse<BatchExecutionResponse> runDailyBatch(
            @RequestParam("targetDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate) {
        BatchExecutionResponse response = settlementJobLauncher.runDaily(targetDate);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @PostMapping("/batch/monthly")
    public CommonResponse<BatchExecutionResponse> runMonthlyBatch(
            @RequestParam("targetMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth targetMonth) {
        BatchExecutionResponse response = settlementJobLauncher.runMonthly(targetMonth);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @GetMapping("/batch/history")
    public CommonResponse<List<BatchExecutionResponse>> getBatchHistory(
            @RequestParam(value = "jobType", required = false) String jobType,
            @RequestParam(value = "count", defaultValue = "20") int count) {
        List<BatchExecutionResponse> history = StringUtils.hasText(jobType)
                ? settlementJobHistoryLauncher.getHistory(jobType, count)
                : settlementJobHistoryLauncher.getAllHistory(count);
        return CommonResponse.success(SuccessCode.OK, history);
    }
}
