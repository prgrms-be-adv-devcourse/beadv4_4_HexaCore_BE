package com.back.product.adapter.in;

import com.back.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 관련 API")
public interface ProductApi {

    @Operation(summary = "브랜드 목록 조회", description = "상품의 브랜드 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "브랜드 목록 조회 성공")
    CommonResponse<?> getBrands();
}
