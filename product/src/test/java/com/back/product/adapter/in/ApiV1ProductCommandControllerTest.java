package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiV1ProductCommandController.class)
@DisplayName("ApiV1ProductController 테스트")
class ApiV1ProductCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private ProductFacade productFacade;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setupObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProductTest {

        @Test
        @DisplayName("상품 생성을 성공한다")
        @WithMockUser
        void createProduct_Success() throws Exception {
            // given
            ProductCreateRequestDto request = RequestFixture.createProductCreateRequest();
            ProductResponseDto response = RequestFixture.createProductResponse();

            given(productFacade.createProduct(any(ProductCreateRequestDto.class))).willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/api/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("CREATED"))
                    .andExpect(jsonPath("$.data.productInfo.productInfoId").value(response.productInfo().productInfoId()))
                    .andExpect(jsonPath("$.data.productInfo.name").value(response.productInfo().name()));

            verify(productFacade).createProduct(any(ProductCreateRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 생성 시 400 Bad Request를 반환한다")
        @WithMockUser
        void createProduct_Fail_Validation() throws Exception {
            // given
            ProductCreateRequestDto request = null;

            // when & then
            mockMvc.perform(
                            post("/api/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).createProduct(any(ProductCreateRequestDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{productInfoId}")
    class UpdateProductTest {

        @Test
        @DisplayName("상품 수정을 성공한다")
        @WithMockUser
        void updateProduct_Success() throws Exception {
            // given
            long productInfoId = 1L;
            ProductUpdateRequestDto request = RequestFixture.createProductUpdateRequest();
            ProductResponseDto response = RequestFixture.createUpdatedProductResponse();

            given(productFacade.updateProduct(anyLong(), any(ProductUpdateRequestDto.class))).willReturn(response);

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/{productInfoId}", productInfoId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.productInfo.productInfoId").value(response.productInfo().productInfoId()))
                    .andExpect(jsonPath("$.data.productInfo.name").value(response.productInfo().name()));

            verify(productFacade).updateProduct(anyLong(), any(ProductUpdateRequestDto.class));
        }

        @Test
        @DisplayName("존재하지 않는 상품 정보 ID로 수정 시 404 Not Found를 반환한다")
        @WithMockUser
        void updateProduct_Fail_NotFound() throws Exception {
            // given
            long productInfoId = 999L;
            ProductUpdateRequestDto request = RequestFixture.createProductUpdateRequest();
            given(productFacade.updateProduct(anyLong(), any(ProductUpdateRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.PRODUCT_INFO_NOT_FOUND));

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/{productInfoId}", productInfoId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    ).andDo(print())
                    .andExpect(status().isNotFound());

            verify(productFacade).updateProduct(anyLong(), any(ProductUpdateRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 수정 시 400 Bad Request를 반환한다")
        @WithMockUser
        void updateProduct_Fail_Validation() throws Exception {
            // given
            long productInfoId = 1L;
            ProductUpdateRequestDto request = null;

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/{productInfoId}", productInfoId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).updateProduct(anyLong(), any(ProductUpdateRequestDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{productInfoId}")
    class DeleteProductTest {

        @Test
        @DisplayName("상품 삭제를 성공한다")
        @WithMockUser
        void deleteProduct_Success() throws Exception {
            // given
            long productInfoId = 1L;
            willDoNothing().given(productFacade).deleteProduct(productInfoId);

            // when & then
            mockMvc.perform(
                            delete("/api/v1/products/{productInfoId}", productInfoId)
                    ).andDo(print())
                    .andExpect(status().isNoContent());

            verify(productFacade).deleteProduct(productInfoId);
        }

        @Test
        @DisplayName("존재하지 않는 상품 정보 ID로 삭제 시 404 Not Found를 반환한다")
        @WithMockUser
        void deleteProduct_Fail_NotFound() throws Exception {
            // given
            long productInfoId = 999L;
            willThrow(new CustomException(FailureCode.PRODUCT_INFO_NOT_FOUND))
                    .given(productFacade).deleteProduct(productInfoId);

            // when & then
            mockMvc.perform(
                            delete("/api/v1/products/{productInfoId}", productInfoId)
                    ).andDo(print())
                    .andExpect(status().isNotFound());

            verify(productFacade).deleteProduct(productInfoId);
        }
    }
}
