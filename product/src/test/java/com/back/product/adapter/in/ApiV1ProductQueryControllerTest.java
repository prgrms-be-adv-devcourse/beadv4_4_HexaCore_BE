package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.ProductFacade;
import com.back.product.dto.enums.ProductSortType;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductResponseDto;
import com.back.product.dto.response.ProductSearchListResponseDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.util.RequestFixture;
import com.back.security.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiV1ProductQueryController.class)
@DisplayName("ApiV1ProductQueryController 테스트")
class ApiV1ProductQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private ProductFacade productFacade;

    @BeforeEach
    void setupObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productInfoId}")
    class GetProductDetailTest {

        @Test
        @DisplayName("상품 상세 조회를 성공한다")
        @WithMockUser
        void getProductDetail_Success() throws Exception {
            // given
            long productInfoId = 1L;
            ProductResponseDto response = RequestFixture.createProductResponse();
            given(productFacade.getProductDetail(productInfoId)).willReturn(response);

            // when & then
            mockMvc.perform(
                            get("/api/v1/products/{productInfoId}", productInfoId)
                    ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.productInfo.productInfoId").value(response.productInfo().productInfoId()))
                    .andExpect(jsonPath("$.data.products[0].productId").value(response.products().getFirst().productId()));

            verify(productFacade).getProductDetail(productInfoId);
        }

        @Test
        @DisplayName("존재하지 않는 상품 정보 ID로 조회 시 404 Not Found를 반환한다")
        @WithMockUser
        void getProductDetail_Fail_NotFound() throws Exception {
            // given
            long productInfoId = 999L;
            given(productFacade.getProductDetail(productInfoId))
                    .willThrow(new CustomException(FailureCode.PRODUCT_INFO_NOT_FOUND));

            // when & then
            mockMvc.perform(
                            get("/api/v1/products/{productInfoId}", productInfoId)
                    ).andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_INFO_NOT_FOUND"));

            verify(productFacade).getProductDetail(productInfoId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetProductListTest {

        @Test
        @DisplayName("상품 목록 조회 및 검색을 성공한다")
        @WithMockUser
        void getProductList_Success() throws Exception {
            // given
            ProductSearchResponseDto productSearchResponseDto = ProductSearchResponseDto.builder()
                    .productInfoId(1L)
                    .productName("Test Product")
                    .thumbnailUrl("thumb.jpg")
                    .brandName("Test Brand")
                    .categoryName("Test Category")
                    .releasePrice(BigDecimal.valueOf(30000))
                    .build();

            ProductSearchListResponseDto responseDto = ProductSearchListResponseDto.builder()
                    .products(List.of(productSearchResponseDto))
                    .build();

            given(productFacade.findProductPage(any(ProductSearchRequestDto.class), anyLong(), anyLong())).willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            get("/api/v1/products")
                                    .param("keyword", "Test")
                                    .param("brandIds", "1")
                                    .param("categoryIds", "1")
                                    .param("minPrice", "10000")
                                    .param("maxPrice", "50000")
                                    .param("excludeSoldOut", "true")
                                    .param("sort", "LATEST")
                                    .param("page", "0")
                                    .param("size", "10")
                    ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.products[0].productName").value("Test Product"));

            verify(productFacade).findProductPage(any(ProductSearchRequestDto.class), eq(0L), eq(10L));
        }

        @Test
        @DisplayName("잘못된 정렬 조건으로 조회 시 400 Bad Request를 반환한다")
        @WithMockUser
        void getProductList_Fail_InvalidSort() throws Exception {
            // when & then
            mockMvc.perform(
                            get("/api/v1/products")
                                    .param("sort", "INVALID_SORT")
                                    .param("page", "0")
                                    .param("size", "10")
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).findProductPage(any(), anyLong(), anyLong());
        }
    }
}
