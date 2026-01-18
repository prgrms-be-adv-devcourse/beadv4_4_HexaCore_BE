package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.ProductFacade;
import com.back.product.dto.BrandDto;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.security.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiV1BrandController.class)
@DisplayName("ApiV1BrandController 테스트")
class ApiV1BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private ProductFacade productFacade;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /api/v1/products/brands")
    class GetBrandsTest {
        @Test
        @DisplayName("브랜드 목록 조회를 성공한다")
        @WithMockUser
        void getBrands() throws Exception {
            // given
            given(productFacade.getBrands()).willReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(
                            get("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                    ).andDo(print())
                    .andExpect(status().isOk());

            verify(productFacade).getBrands();
        }
    }


    @Nested
    @DisplayName("POST /api/v1/products/brands")
    class CreateBrandTest {

        @Test
        @DisplayName("브랜드 생성을 성공한다")
        @WithMockUser
        void createBrand_Success() throws Exception {
            // given
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto("New Balance", "logo.png");
            BrandDto responseDto = BrandDto.builder().name("New Balance").logoUrl("logo.png").build();

            given(productFacade.createBrand(any(BrandCreateRequestDto.class))).willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("CREATED"))
                    .andExpect(jsonPath("$.data.brand.name").value("New Balance"))
                    .andExpect(jsonPath("$.data.brand.logoUrl").value("logo.png"));

            verify(productFacade).createBrand(any(BrandCreateRequestDto.class));
        }

        @Test
        @DisplayName("중복된 브랜드 이름으로 생성 시 409 Conflict를 반환한다")
        @WithMockUser
        void createBrand_Fail_DuplicateName() throws Exception {
            // given
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto("Existing Brand", "logo.png");
            given(productFacade.createBrand(any(BrandCreateRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.BRAND_NAME_DUPLICATE));

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isConflict());

            verify(productFacade).createBrand(any(BrandCreateRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 생성 시 400 Bad Request를 반환한다")
        @WithMockUser
        void createBrand_Fail_Validation() throws Exception {
            // given
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto(" ", "logo.png");

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).createBrand(any(BrandCreateRequestDto.class));
        }
    }
}