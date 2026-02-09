package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.in.web.controller.ApiV1ProductQueryController;
import com.back.product.app.facade.ProductFacade;
import com.back.product.dto.model.*;
import com.back.product.dto.request.ProductQueryRequestDto;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductDetailListResponseDto;
import com.back.product.dto.response.ProductDetailResponseDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.dto.model.ProductSearchDto;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
            ProductDetailResponseDto response = RequestFixture.createProductResponse();
            given(productFacade.getProductDetail(productInfoId)).willReturn(response);

            // when & then
            mockMvc.perform(
                            get("/api/v1/products/{productInfoId}", productInfoId)
                    ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.product.productInfo.productInfoId").value(response.product().productInfo().productInfoId()))
                    .andExpect(jsonPath("$.data.product.products[0].productId").value(response.product().products().getFirst().productId()));

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
    class SearchProductsTest {

        @Test
        @DisplayName("상품 목록 조회 및 검색을 성공한다")
        @WithMockUser
        void searchProducts_Success() throws Exception {
            // given
            ProductSearchDto productSearchDto = ProductSearchDto.builder()
                    .productInfoId(1L)
                    .productName("Test Product")
                    .thumbnailUrl("thumb.jpg")
                    .brandName("Test Brand")
                    .categoryName("Test Category")
                    .releasePrice(BigDecimal.valueOf(30000))
                    .build();

            ProductSearchResponseDto responseDto = ProductSearchResponseDto.builder()
                    .products(List.of(productSearchDto))
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
        void searchProducts_Fail_InvalidSort() throws Exception {
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

    @Nested
    @DisplayName("GET /api/v1/products/variants")
    class GetProductsTest {

        @Test
        @DisplayName("유효한 상품 ID 리스트로 상품 목록 조회를 성공한다")
        void getProducts_Success() throws Exception {
            // given
            List<ProductDetailResponseDto> products = Arrays.asList(
                    ProductDetailResponseDto.builder()
                            .product(
                                    ProductDetailDto.builder()
                                        .productInfo(ProductInfoDto.builder()
                                                .productInfoId(1L)
                                                .name("Test Product 1")
                                                .brand(BrandDto.builder().name("Brand 1").build())
                                                .category(CategoryDto.builder().name("category1").build())
                                                .releasePrice(BigDecimal.valueOf(10000))
                                                .build())
                                        .products(List.of(ProductDto.builder()
                                                .productId(1L)
                                                .inventory(10L)
                                                .imageUrls(List.of("image1.jpg"))
                                                .options(List.of(
                                                        OptionDto.builder()
                                                                .group(OptionDto.GroupDto.builder().id(1L).name("size").build())
                                                                .values(List.of(OptionDto.ValueDto.builder().id(1L).name("M").build()))
                                                                .build(),
                                                        OptionDto.builder()
                                                                .group(OptionDto.GroupDto.builder().id(2L).name("color").build())
                                                                .values(List.of(OptionDto.ValueDto.builder().id(2L).name("red").build()))
                                                                .build()
                                                ))
                                                .build())
                                        ).build()
                            ).build(),
                    ProductDetailResponseDto.builder()
                            .product(
                                    ProductDetailDto.builder()
                                            .productInfo(ProductInfoDto.builder()
                                                    .productInfoId(2L)
                                                    .name("Test Product 2")
                                                    .brand(BrandDto.builder().name("Brand 2").build())
                                                    .category(CategoryDto.builder().name("category2").build())
                                                    .releasePrice(BigDecimal.valueOf(20000))
                                                    .build())
                                            .products(List.of(ProductDto.builder()
                                                    .productId(2L)
                                                    .inventory(20L)
                                                    .imageUrls(List.of("image2.jpg"))
                                                    .options(List.of(
                                                            OptionDto.builder()
                                                                    .group(OptionDto.GroupDto.builder().id(1L).name("size").build())
                                                                    .values(List.of(OptionDto.ValueDto.builder().id(3L).name("L").build()))
                                                                    .build(),
                                                            OptionDto.builder()
                                                                    .group(OptionDto.GroupDto.builder().id(2L).name("color").build())
                                                                    .values(List.of(OptionDto.ValueDto.builder().id(4L).name("blue").build()))
                                                                    .build()
                                                    ))
                                                    .build())
                                            ).build()
                            ).build(),
                    ProductDetailResponseDto.builder()
                            .product(
                                    ProductDetailDto.builder()
                                            .productInfo(ProductInfoDto.builder()
                                                    .productInfoId(3L)
                                                    .name("Test Product 3")
                                                    .brand(BrandDto.builder().name("Brand 3").build())
                                                    .category(CategoryDto.builder().name("category3").build())
                                                    .releasePrice(BigDecimal.valueOf(30000))
                                                    .build())
                                            .products(List.of(ProductDto.builder()
                                                    .productId(3L)
                                                    .inventory(30L)
                                                    .imageUrls(List.of("image3.jpg"))
                                                    .options(List.of(
                                                            OptionDto.builder()
                                                                    .group(OptionDto.GroupDto.builder().id(1L).name("size").build())
                                                                    .values(List.of(OptionDto.ValueDto.builder().id(5L).name("S").build()))
                                                                    .build(),
                                                            OptionDto.builder()
                                                                    .group(OptionDto.GroupDto.builder().id(2L).name("color").build())
                                                                    .values(List.of(OptionDto.ValueDto.builder().id(6L).name("green").build()))
                                                                    .build()
                                                    ))
                                                    .build())
                                            ).build()
                            ).build()
            );
            ProductDetailListResponseDto responseDto = ProductDetailListResponseDto.builder().products(
                    products.stream().map(ProductDetailResponseDto::product).toList()
            ).build();

            given(productFacade.getProducts(any(ProductQueryRequestDto.class))).willReturn(responseDto);

            // when & then
            mockMvc.perform(get("/api/v1/products/variants")
                            .param("productIds", "1")
                            .param("productIds", "2")
                            .param("productIds", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.products.length()").value(products.size()))
                    .andExpect(jsonPath("$.data.products[0].products[0].productId").value(1L))
                    .andExpect(jsonPath("$.data.products[0].productInfo.name").value("Test Product 1"))
                    .andDo(print());

            verify(productFacade).getProducts(any(ProductQueryRequestDto.class));
        }

        @Test
        @DisplayName("상품 ID 리스트가 비어있을 때 Bad Request을 반환한다")
        void getProducts_Failed_Enable_Values() throws Exception {
            // given & when & then
            mockMvc.perform(get("/api/v1/products/variants")
                            .param("productIds", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());

            verify(productFacade, never()).getProducts(any(ProductQueryRequestDto.class));
        }

        @Test
        @DisplayName("상품 ID 리스트 중 하나라도 값을 찾지 못하면 PRODUCT_NOT_FOUND 에러를 반환한다.")
        void getProducts_Failed_Not_Found_Product() throws Exception {
            // given
            given(productFacade.getProducts(any(ProductQueryRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.ENTITY_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/products/variants")
                            .param("productIds", "1", "999") // 999 is a non-existent product ID
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(FailureCode.ENTITY_NOT_FOUND.name()))
                    .andDo(print());

            verify(productFacade).getProducts(any(ProductQueryRequestDto.class));
        }
    }
}
