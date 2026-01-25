package com.back.settlement.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.settlement.app.dto.response.BatchExecutionResponse;
import com.back.settlement.app.dto.response.SettlementDashboardResponse;
import com.back.settlement.app.dto.response.SettlementLogResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Settlement Admin", description = "정산 관리 API (관리자 전용)")
public interface SettlementAdminApi {

    @Operation(
            summary = "정산 대시보드 조회",
            description = "정산 현황 대시보드를 조회합니다. 총 정산서 수, 상태별 수를 제공합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementDashboardResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    CommonResponse<SettlementDashboardResponse> getDashboard();

    @Operation(
            summary = "정산서 목록 조회",
            description = "정산서 목록을 조회합니다. 정산 상태, 판매자ID, 기간으로 필터링할 수 있습니다. 필터 없이 호출하면 전체 목록을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    CommonResponse<Page<SettlementResponse>> getSettlements(
            @Parameter(description = "정산 상태", example = "PENDING") @RequestParam(required = false) SettlementStatus status,
            @Parameter(description = "판매자 ID", example = "1") @RequestParam(required = false) Long sellerId,
            @Parameter(description = "시작일시", example = "2024-01-01T00:00:00") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "종료일시", example = "2024-01-31T23:59:59") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(hidden = true) Pageable pageable
    );

    @Operation(
            summary = "정산서 상세 조회",
            description = "특정 정산서의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "정산서를 찾을 수 없음", content = @Content)
            }
    )
    CommonResponse<SettlementResponse> getSettlement(
            @Parameter(description = "정산서 ID", required = true, example = "1") @PathVariable("settlementId") Long settlementId
    );

    @Operation(
            summary = "특정 정산서 상태 변경 이력 조회",
            description = "특정 정산서의 상태 변경 로그를 시간순으로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementLogResponse.class),
                                    examples = @ExampleObject(
                                            name = "상태 변경 이력 조회 성공 예시",
                                            value = """
                                                {
                                                  "status": 200,
                                                  "message": "OK",
                                                  "data": [
                                                    {
                                                      "logId": 1,
                                                      "settlementId": 1,
                                                      "previousStatus": null,
                                                      "newStatus": "PENDING",
                                                      "reason": "정산서 생성",
                                                      "actorType": "BATCH",
                                                      "actorId": null,
                                                      "createdAt": "2024-03-01T02:00:00"
                                                    },
                                                    {
                                                      "logId": 2,
                                                      "settlementId": 1,
                                                      "previousStatus": "PENDING",
                                                      "newStatus": "IN_PROGRESS",
                                                      "reason": "정산 처리 시작",
                                                      "actorType": "BATCH",
                                                      "actorId": null,
                                                      "createdAt": "2024-03-15T02:00:00"
                                                    }
                                                  ]
                                                }
                                                """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "정산서를 찾을 수 없음", content = @Content)
            }
    )
    CommonResponse<List<SettlementLogResponse>> getSettlementLogs(
            @Parameter(description = "정산서 ID", required = true, example = "1")
            @PathVariable("settlementId") Long settlementId
    );

    @Operation(
            summary = "전체 정산 상태 변경 이력 조회",
            description = "모든 정산서의 상태 변경 로그를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementLogResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
            }
    )
    CommonResponse<List<SettlementLogResponse>> getAllLogs();

    @Operation(
            summary = "정산 배치 수동 실행",
            description = "특정 월의 정산 배치를 수동으로 실행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "배치 실행 완료",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "배치 실행 성공 예시",
                                            value = """
                                                {
                                                  "status": 200,
                                                  "message": "OK",
                                                  "data": {
                                                    "jobId": 1,
                                                    "status": "COMPLETED",
                                                    "targetMonth": "2024-01",
                                                    "startTime": "2024-01-15T02:00:00",
                                                    "endTime": "2024-01-15T02:05:30",
                                                    "processedCount": 150
                                                  }
                                                }
                                                """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "500", description = "배치 실행 실패", content = @Content)
            }
    )
    CommonResponse<BatchExecutionResponse> runSettlementBatch(
            @Parameter(description = "정산 대상 월 (yyyy-MM 형식)", example = "2024-01")
            @RequestParam("targetMonth") YearMonth targetMonth
    );
}
