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
            summary = "판매자 정산 내역 조회",
            description = "로그인한 판매자의 정산 내역을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "정산 내역 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementResponse.class),
                                    examples = @ExampleObject(
                                            name = "정산 내역 리스트 조회 성공 예시",
                                            value = """
                                                {
                                                  "status": 200,
                                                  "message": "OK",
                                                  "data": [
                                                    {
                                                      "settlementItemId": 1,
                                                      "sellerId": 1001,
                                                      "status": "COMPLETED",
                                                      "expectedAt": "2024-03-15T00:00:00",
                                                      "startAt": "2024-02-01T00:00:00",
                                                      "endAt": "2024-02-29T23:59:59",
                                                      "completedAt": "2024-03-15T11:00:00",
                                                      "totalSalesAmount": 2300000,
                                                      "totalFeeAmount": 230000,
                                                      "totalNetAmount": 2070000
                                                    },
                                                    {
                                                      "settlementItemId": 2,
                                                      "sellerId": 1,
                                                      "status": "COMPLETED",
                                                      "expectedAt": "2024-03-15T00:00:00",
                                                      "startAt": "2024-02-01T00:00:00",
                                                      "endAt": "2024-02-29T23:59:59",
                                                      "completedAt": "2024-03-15T11:00:00",
                                                      "totalSalesAmount": 2300000,
                                                      "totalFeeAmount": 230000,
                                                      "totalNetAmount": 2070000
                                                    }
                                                  ]
                                                }
                                                """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
            }
    )
    CommonResponse<List<SettlementResponse>> getSettlements();

    @Operation(
            summary = "판매자 정산 상품 조회",
            description = "로그인한 판매자의 정산 상품을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "정산 상품 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SettlementItemResponse.class),
                                    examples = @ExampleObject(
                                            name = "정산 내역 조회 성공 예시",
                                            value = """
                                                {
                                                  "status": 200,
                                                  "message": "OK",
                                                  "data": {
                                                    "settlementItemId": 3,
                                                    "orderId": 1003,
                                                    "productId": 103,
                                                    "buyerId": 203,
                                                    "sellerId": 1,
                                                    "status": "REFUNDED",
                                                    "salesAmount": 30000,
                                                    "feeAmount": 3000,
                                                    "netAmount": 27000,
                                                    "transactionAt": "2024-01-12T16:45:00"
                                                  }
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