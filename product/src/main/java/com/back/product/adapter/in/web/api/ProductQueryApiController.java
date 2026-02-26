package com.back.product.adapter.in.web.api;

import com.back.common.response.CommonResponse;
import com.back.product.dto.request.ProductQueryRequestDto;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.request.PageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product Query", description = "상품 조회 관련 API")
public interface ProductQueryApiController {
    @Operation(summary = "상품 상세 조회", description = """
            특정 상품 정보(ProductInfo)를 기준으로 하위의 모든 변체(Product) 정보를 조회합니다.
            출시일, 브랜드, 카테고리 등 공통 정보와 각 변체별 옵션 조합을 함께 제공합니다.
    """)
    @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "상품 정보를 찾을 수 없음", content = @Content)
    CommonResponse<?> getProductDetail(Long productInfoId);

    @Operation(summary = "상품 목록 조회 및 검색", description = """
            Elasticsearch를 활용하여 고성능 상품 검색을 제공합니다.
            1. 키워드: 상품명, 브랜드명, 카테고리명, 옵션명에 대한 형태소 분석 검색
            2. 필터링: 브랜드 ID 리스트, 카테고리 ID 리스트 (각 리스트 내 요소는 OR 조건으로 동작)
            3. 가격 범위: minPrice 이상, maxPrice 이하 필터링
            4. 정렬: 최신순(기본), 낮은가격순, 높은가격순 지원
    """)
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    CommonResponse<?> searchProducts(ProductSearchRequestDto request);

    @Operation(summary = "유사 상품 조회", description = """
            선택한 상품과 특징이 유사한 상품 5~50개를 추천합니다.
            AI 임베딩 벡터 기반의 KNN(K-Nearest Neighbors) 알고리즘을 활용합니다.
    """)
    @ApiResponse(responseCode = "200", description = "추천 상품 목록 조회 성공")
    @ApiResponse(responseCode = "404", description = "기준 상품 또는 임베딩 데이터를 찾을 수 없음", content = @Content)
    CommonResponse<?> findSimilarProducts(Long productInfoId, PageRequestDto request);

    @Operation(summary = "상품 다중 ID 조회", description = """
            여러 개의 상품 ID를 리스트로 받아 해당 상품들의 정보를 일괄 조회합니다.
            주로 장바구니나 결제 페이지에서 사용됩니다.
    """)
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    @ApiResponse(responseCode = "404", description = "요청한 ID 중 존재하지 않는 상품이 포함됨", content = @Content)
    CommonResponse<?> getProducts(ProductQueryRequestDto request);
}
