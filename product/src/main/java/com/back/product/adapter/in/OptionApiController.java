package com.back.product.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 관련 API")
public interface OptionApiController {

    @Operation(summary = "상품 옵션 목록 조회", description = "상품의 옵션 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상품 옵션 목록 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> getOptions();

    @Operation(summary = "옵션 생성", description = "새로운 옵션 그룹을 값을 포함하여 생성합니다.")
    @ApiResponse(responseCode = "201", description = "새로운 옵션 그룹 생성 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> createOptions(OptionCreateRequestDto request);

    @Operation(summary = "옵션 추가", description = "기존의 옵션 그룹에 값을 추가합니다.")
    @ApiResponse(responseCode = "201", description = "새로운 옵션 값 생성 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> appendOptions(Long optionGroupId, OptionAppendRequestDto request);

    @Operation(summary = "옵션 그룹 수정", description = "기존의 옵션 그룹의 값을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "옵션 그룹 수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> modifyOptionGroup(Long optionGroupId, OptionGroupModifyRequestDto request);

    @Operation(summary = "옵션 값 수정", description = "기존의 옵션 값을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "옵션 값 수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> modifyOptionValue(Long optionValueId, OptionValueModifyRequestDto request);

    @Operation(summary = "옵션 그룹 삭제", description = """
        기존의 옵션 그룹을 삭제합니다.
        삭제 시, 그룹 내의 값들을 모두 확인해서 사용되는 상품이 있다면, 삭제를 진행하지 않습니다.
    """)
    @ApiResponse(responseCode = "204", description = "옵션 값 수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> deleteOptionGroup(Long optionGroupId);
}
