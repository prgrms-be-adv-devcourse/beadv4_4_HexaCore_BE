package com.back.product.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 관련 API")
public interface ProductApiController {
    @Operation(summary = "상품 생성", description = """
            새로운 상품을 생성합니다.
            상품(Product) 생성 시, 옵션의 개수 N개 만큼 생성합니다.
            옵션은 기존에 존재하는 데이터를 선택할 수 있도록 합니다.
            생성되는 상품마다 이미지를 N개 설정할 수 있습니다.
    """)
    @ApiResponse(responseCode = "201", description = "상품 생성 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> createProduct(ProductCreateRequestDto request);

    @Operation(summary = "상품 수정", description = """
            존재하는 상품을 수정합니다.
            상품 공통 정보(ProductInfo)를 수정할 수 있습니다.
            상품의 옵션(ProductOptionValues)와 이미지(ProductImage)를 수정할 수 있습니다.
            상품의 옵션 및 이미지(Variants)의 입력 값은 아래와 같습니다.
            - productId의 유무에 따라, Variant 수정/추가를 합니다.
            - 옵션 및 이미지는 기존의 데이터들을 삭제한 후 새로운 데이터를 추가합니다.
            - productId에 매칭되지 않는 기존 Product들은 삭제합니다.
    """)
    @ApiResponse(responseCode = "200", description = "상품 수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> updateProduct(Long productInfoId, ProductUpdateRequestDto request);

    @Operation(summary = "상품 삭제", description = """
            상품을 삭제합니다.
            상품의 기본 정보 (ProductInfo)에 매칭되는 모든 상품들(Product)을 삭제합니다.
            삭제되는 상품에 매칭되는 상품 옵션(ProductOptionValues) 및 이미지(ProductImage)를 삭제합니다.
            모든 삭제는 Soft Delete를 준수합니다.
    """)
    @ApiResponse(responseCode = "204", description = "상품 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> deleteProduct(Long productInfoId);
}
