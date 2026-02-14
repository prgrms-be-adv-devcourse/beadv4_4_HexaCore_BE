package com.back.product.adapter.in.web.api;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.ProductQueryRequestDto;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.request.PageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 관련 API")
public interface ProductQueryApiController {
    @Operation(summary = "상품 상세 조회", description = """
            상품의 상세 정보를 조회합니다.
            상품 기본 정보 (ProductInfo)를 기반으로 이로 파생된 상세 상품(Product)들을 모두 조회합니다.
            ex. A 상품(ProductInfo)의 색상, 사이즈(ProductOptionValues)에 따른 개별 상품(Product) 조회
    """)
    @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공")
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> getProductDetail(Long productInfoId);

    @Operation(summary = "상품 목록 조회 및 검색", description = """
            1. 검색어 (전문 검색) : keyword
            2. 필터링 조건 (다중 선택 가능) (OR 조건) : brandIds, categoryIds
            3. 범위 필터링 (가격) : minPrice, maxPrice
            4. 정렬 조건 : sort (ENUM ProductSortType)
            5. 페이징 : page (페이지 번호), size (항목 개수)
    """)
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> searchProducts(ProductSearchRequestDto request);

    @Operation(summary = "유사 상품 조회", description = """
            특정 상품과 유사한 상품들을 조회합니다.
            KNN 알고리즘을 활용하여 유사한 상품들을 추천합니다.
            추천할 상품 수는 'size' 파라미터로 지정할 수 있습니다.
    """)
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> findSimilarProducts(Long productInfoId, PageRequestDto request);

    @Operation(summary = "단일 항목 상품 다중 조회", description = """
            단일 항목 상품의 정보를 다중 조회합니다.
            한 상품의 기본 정보(ProductInfo)를 포함한 단일 상품의 정보를 조회합니다.
            요청 시, 상품의 ID를 리스트로 받아 다중 조회 처리합니다.
            ex. 상품의 기본 정보 + 재고 + 옵션 (사이즈 + 색상 + ...) + ... 
    """)
    @ApiResponse(responseCode = "200", description = "단일 항목 상품 다중 조회 성공")
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> getProducts(ProductQueryRequestDto request);
}
