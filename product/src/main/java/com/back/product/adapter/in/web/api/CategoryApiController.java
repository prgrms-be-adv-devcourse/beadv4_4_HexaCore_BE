package com.back.product.adapter.in.web.api;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.dto.request.CategoryListCreateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Category", description = "카테고리 관련 API")
public interface CategoryApiController {
    @Operation(summary = "카테고리 목록 조회", description = "상품 카테고리 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    CommonResponse<?> getCategories(Integer page, Integer size);

    @Operation(summary = "카테고리 생성", description = "새로운 상품 카테고리를 생성합니다. 이미 존재하는 카테고리명은 무시됩니다.")
    @ApiResponse(responseCode = "201", description = "카테고리 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    CommonResponse<?> createCategories(CategoryListCreateRequestDto request);

    @Operation(summary = "카테고리 수정", description = "기존 카테고리의 이름 또는 이미지를 수정합니다. 중복된 이름으로의 수정은 제한됩니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 수정 성공")
    @ApiResponse(responseCode = "400", description = "이미 존재하는 카테고리 이름 (CATEGORY_NAME_DUPLICATE)", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리", content = @Content)
    CommonResponse<?> modifyCategory(Long categoryId, CategoryDataRequestDto request);

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. 해당 카테고리에 속한 상품이 하나라도 있다면 삭제할 수 없습니다.")
    @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "409", description = "상품에서 사용 중인 카테고리 삭제 불가 (CATEGORY_IN_USE)", content = @Content)
    CommonResponse<?> deleteCategory(Long categoryId);
}
