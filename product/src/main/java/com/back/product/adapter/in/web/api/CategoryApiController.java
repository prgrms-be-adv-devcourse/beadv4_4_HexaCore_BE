package com.back.product.adapter.in.web.api;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.dto.request.CategoryListCreateRequestDto;
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
    CommonResponse<?> getCategories(Long page, Long size);

    @Operation(summary = "카테고리 생성", description = "새로운 상품 카테고리를 생성합니다. 다중 생성이 가능합니다.")
    @ApiResponse(responseCode = "201", description = "카테고리 생성 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> createCategories(CategoryListCreateRequestDto request);

    @Operation(summary = "카테고리 수정", description = "기존의 상품 카테고리를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> modifyCategory(Long categoryId, CategoryDataRequestDto request);

    @Operation(summary = "카테고리 삭제", description = "기존의 상품 카테고리를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> deleteCategory(Long categoryId);
}
