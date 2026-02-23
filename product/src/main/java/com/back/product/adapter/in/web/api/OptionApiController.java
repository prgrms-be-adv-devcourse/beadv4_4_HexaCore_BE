package com.back.product.adapter.in.web.api;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.OptionAppendRequestDto;
import com.back.product.dto.request.OptionGroupModifyRequestDto;
import com.back.product.dto.request.OptionListCreateRequestDto;
import com.back.product.dto.request.OptionValueModifyRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Option", description = "옵션 관련 API")
public interface OptionApiController {

    @Operation(summary = "상품 옵션 목록 조회", description = "시스템에 등록된 모든 옵션 그룹과 그에 속한 값들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상품 옵션 목록 조회 성공")
    CommonResponse<?> getOptions();

    @Operation(summary = "옵션 생성", description = "새로운 옵션 그룹을 생성하고 초기 값들을 등록합니다. 이미 존재하는 그룹명일 경우 기존 그룹에 값만 추가됩니다.")
    @ApiResponse(responseCode = "201", description = "새로운 옵션 그룹 생성 또는 추가 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    CommonResponse<?> createOptions(OptionListCreateRequestDto request);

    @Operation(summary = "옵션 값 추가", description = "기존의 옵션 그룹에 새로운 옵션 값들을 추가합니다.")
    @ApiResponse(responseCode = "201", description = "새로운 옵션 값 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "존재하지 않는 옵션 그룹 (OPTION_GROUP_NOT_FOUND)", content = @Content)
    CommonResponse<?> appendOptions(Long optionGroupId, OptionAppendRequestDto request);

    @Operation(summary = "옵션 그룹 수정", description = "기존의 옵션 그룹 명칭을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "옵션 그룹 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "존재하지 않는 옵션 그룹", content = @Content)
    CommonResponse<?> modifyOptionGroup(Long optionGroupId, OptionGroupModifyRequestDto request);

    @Operation(summary = "옵션 값 수정", description = "기존의 옵션 값 명칭을 수정하거나 소속 그룹을 변경합니다.")
    @ApiResponse(responseCode = "200", description = "옵션 값 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "존재하지 않는 옵션 값 또는 그룹", content = @Content)
    CommonResponse<?> modifyOptionValue(Long optionValueId, OptionValueModifyRequestDto request);

    @Operation(summary = "옵션 그룹 삭제", description = """
        기존의 옵션 그룹을 삭제합니다.
        삭제 시, 해당 그룹의 어떤 옵션 값이라도 상품(Product)에 사용되고 있다면 삭제가 거부됩니다.
    """)
    @ApiResponse(responseCode = "204", description = "옵션 그룹 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "409", description = "상품에서 사용 중인 옵션 그룹 삭제 불가 (OPTION_GROUP_IN_USE)", content = @Content)
    CommonResponse<?> deleteOptionGroup(Long optionGroupId);

    @Operation(summary = "옵션 값 삭제", description = """
        기존의 옵션 값을 삭제합니다.
        삭제 시, 해당 옵션 값이 상품(Product)에서 참조되고 있다면 삭제가 거부됩니다.
    """)
    @ApiResponse(responseCode = "204", description = "옵션 값 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "409", description = "상품에서 사용 중인 옵션 값 삭제 불가 (OPTION_VALUE_IN_USE)", content = @Content)
    CommonResponse<?> deleteOptionValue(Long optionValueId);
}
