package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.ProductFacade;
import com.back.product.dto.response.ProductResponseDto;
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

import static org.mockito.BDDMockito.*;
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
}
