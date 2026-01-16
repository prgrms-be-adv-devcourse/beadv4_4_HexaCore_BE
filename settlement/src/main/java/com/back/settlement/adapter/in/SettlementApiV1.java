package com.back.settlement.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Settlement", description = "정산 관련 API")
public interface SettlementApiV1 {

    @Operation(
            summary = "판매자 정산 내역 페이징 조회",
            description = "로그인한 판매자의 정산 내역을 페이지네이션으로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "정산 내역 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "status": 200,
                                                "message": "OK",
                                                "data": {
                                                    "settlementItemId": 1,
                                                    "orderId": 1001,
                                                    "productId": 500,
                                                    "buyerId": 200,
                                                    "sellerId": 100,
                                                    "status": "COMPLETED",
                                                    "salesAmount": 50000.00,
                                                    "feeAmount": 5000.00,
                                                    "netAmount": 45000.00,
                                                    "transactionAt": "2024-01-15T14:30:00"
                                                }
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
            }
    )
    CommonResponse<List<SettlementResponse>> getSettlements();

    @Operation(
            summary = "판매자 정산 내역 조회",
            description = "로그인한 판매자의 정산 내역을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "정산 내역 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementItemResponse.class),
                                    examples = @ExampleObject(
                                            name = "정산 내역 조회 성공 예시",
                                            value = """
                                                {
                                                  "status": 200,
                                                  "message": "OK",
                                                  "data": [
                                                    {
                                                      "settlementId": 1,
                                                      "sellerId": 100,
                                                      "status": "COMPLETED",
                                                      "totalSalesAmount": 50000,
                                                      "totalFeeAmount": 5000,
                                                      "totalNetAmount": 45000,
                                                      "createdAt": "2024-01-15T14:30:00"
                                                    }
                                                  ]
                                                }
                                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "해당 정산 항목에 대한 접근 권한이 없습니다.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "정산 항목을 찾을 수 없습니다.", content = @Content),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
            }
    )
    CommonResponse<SettlementItemResponse> getSettlementItem(
            @PathVariable("settlementItemId") Long settlementItemId
    );
}