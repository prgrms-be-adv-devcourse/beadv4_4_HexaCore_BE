package com.back.product.adapter.in.web.api;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.dto.request.BrandListCreateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 관련 API")
public interface BrandApiController {

    @Operation(summary = "브랜드 목록 조회", description = "상품의 브랜드 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "브랜드 목록 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> getBrands(Long page, Long size);

    @Operation(summary = "브랜드 생성", description = "새로운 브랜드를 생성합니다. 다중 생성이 가능합니다.")
    @ApiResponse(responseCode = "201", description = "브랜드 생성 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> createBrands(BrandListCreateRequestDto request);

    @Operation(summary = "브랜드 수정", description = "기존의 브랜드를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "브랜드 수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> modifyBrand(Long brandId, BrandDataRequestDto request);

    @Operation(summary = "브랜드 삭제", description = "기존의 브랜드를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "브랜드 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> deleteBrand(Long brandId);
}
