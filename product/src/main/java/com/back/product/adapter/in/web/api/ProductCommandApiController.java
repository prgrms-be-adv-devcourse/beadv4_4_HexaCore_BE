package com.back.product.adapter.in.web.api;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product Command", description = "상품 CUD 관련 API")
public interface ProductCommandApiController {
    @Operation(summary = "상품 생성", description = """
            새로운 상품 정보를 생성합니다.
            - ProductInfo(공통 정보): 브랜드, 카테고리, 상품명, 모델코드를 포함합니다.
            - Variants(개별 상품): 공통 정보를 기반으로 색상, 사이즈 등 옵션 조합별 실제 상품들을 생성합니다.
            - 이미지: 각 개별 상품별로 전용 이미지를 설정할 수 있습니다.
    """)
    @ApiResponse(responseCode = "201", description = "상품 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 입력 값", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "참조된 브랜드 또는 카테고리를 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "409", description = "이미 존재하는 상품 코드 (DUPLICATE_PRODUCT_INFO)", content = @Content)
    CommonResponse<?> createProduct(ProductCreateRequestDto request);

    @Operation(summary = "상품 수정", description = """
            존재하는 상품 정보를 수정합니다.
            - ProductInfo의 공통 속성을 변경할 수 있습니다.
            - Variants 수정 규칙:
              1. request의 productId가 기존에 존재하면 해당 변체를 업데이트합니다.
              2. productId가 없으면 새로운 변체를 추가 생성합니다.
              3. request에 포함되지 않은 기존의 모든 변체는 삭제(Soft Delete) 처리됩니다.
    """)
    @ApiResponse(responseCode = "200", description = "상품 수정 성공")
    @ApiResponse(responseCode = "400", description = "입력 데이터 논리 오류 (예: 잘못된 productId)", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "수정할 상품 정보를 찾을 수 없음", content = @Content)
    CommonResponse<?> updateProduct(Long productInfoId, ProductUpdateRequestDto request);

    @Operation(summary = "상품 삭제", description = """
            상품 정보(ProductInfo) 및 그와 연결된 모든 하위 데이터(Product, Image, Option mapping)를 삭제합니다.
            모든 삭제는 데이터 보존을 위해 Soft Delete(삭제 일시 기록) 방식으로 처리됩니다.
    """)
    @ApiResponse(responseCode = "204", description = "상품 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "삭제할 상품 정보를 찾을 수 없음", content = @Content)
    CommonResponse<?> deleteProduct(Long productInfoId);
}
