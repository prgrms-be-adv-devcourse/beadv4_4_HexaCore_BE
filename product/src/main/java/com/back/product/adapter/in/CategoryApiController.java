package com.back.product.adapter.in;

import com.back.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 관련 API")
public interface CategoryApiController {
    @Operation(summary = "카테고리 목록 조회", description = "상품의 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> getCategories();
}
