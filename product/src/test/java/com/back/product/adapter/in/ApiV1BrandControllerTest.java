package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.ProductFacade;
import com.back.product.dto.BrandDto;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.request.BrandModifyRequestDto;
import com.back.product.dto.response.BrandListResponseDto;
import com.back.product.dto.response.BrandResponseDto;
import com.back.security.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
            BrandCreateRequestDto.BrandDto newBrand1 = new BrandCreateRequestDto.BrandDto("New Balance", "https://example.com/logo.png");
            BrandCreateRequestDto.BrandDto newBrand2 = new BrandCreateRequestDto.BrandDto("Nike", "https://example.com/logo2.png");
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto(List.of(newBrand1, newBrand2));

            BrandListResponseDto responseDto = BrandListResponseDto.builder()
                    .brands(List.of(
                            new BrandDto(1L, "New Balance", "https://example.com/logo.png"),
                            new BrandDto(2L, "Nike", "https://example.com/logo2.png")
                    )).build();

            given(productFacade.createBrands(any(BrandCreateRequestDto.class))).willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("CREATED"))
                    .andExpect(jsonPath("$.data.brands.length()").value(2))
                    .andExpect(jsonPath("$.data.brands[0].name").value("New Balance"))
                    .andExpect(jsonPath("$.data.brands[1].name").value("Nike"));

            verify(productFacade).createBrands(any(BrandCreateRequestDto.class));
        }

        @Test
        @DisplayName("요청에 중복된 브랜드 이름이 포함되어 있어도, 생성 가능한 브랜드만 생성하고 201 Created를 반환한다")
        @WithMockUser
        void createBrand_Filter_DuplicateName() throws Exception {
            // given
            BrandCreateRequestDto.BrandDto existingBrand = new BrandCreateRequestDto.BrandDto("Existing Brand", "https://example.com/logo_exist.png");
            BrandCreateRequestDto.BrandDto newBrand = new BrandCreateRequestDto.BrandDto("New Brand", "https://example.com/logo_new.png");
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto(List.of(existingBrand, newBrand));

            // UseCase에서 중복을 걸러내고, 새로 생성된 브랜드만 반환
            BrandListResponseDto responseDto = BrandListResponseDto.builder()
                    .brands(List.of(
                            new BrandDto(1L, "New Brand", "https://example.com/logo_new.png")
                    )).build();


            given(productFacade.createBrands(any(BrandCreateRequestDto.class)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.brands.length()").value(1))
                    .andExpect(jsonPath("$.data.brands[0].name").value("New Brand"));

            verify(productFacade).createBrands(any(BrandCreateRequestDto.class));
        }


        @Test
        @DisplayName("유효하지 않은 요청 값으로 생성 시 400 Bad Request를 반환한다")
        @WithMockUser
        void createBrand_Fail_Validation() throws Exception {
            // given
            BrandCreateRequestDto.BrandDto invalidBrand = new BrandCreateRequestDto.BrandDto(" ", "invalid-url");
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto(List.of(invalidBrand));


            // when & then
            mockMvc.perform(
                            post("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).createBrands(any(BrandCreateRequestDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/brands/{brandId}")
    class ModifyBrandTest {

        private final Long BRAND_ID = 1L;

        @Test
        @DisplayName("브랜드 수정을 성공한다")
        @WithMockUser
        void modifyBrand_Success() throws Exception {
            // given
            BrandModifyRequestDto requestDto = BrandModifyRequestDto.builder()
                    .name("Modified Brand")
                    .imageUrl("https://example.com/modified_logo.png")
                    .build();
            BrandResponseDto responseDto = BrandResponseDto.builder()
                    .brand(new BrandDto(BRAND_ID, "Modified Brand", "https://example.com/modified_logo.png"))
                    .build();

            given(productFacade.modifyBrand(eq(BRAND_ID), any(BrandModifyRequestDto.class))).willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.brand.brandId").value(BRAND_ID))
                    .andExpect(jsonPath("$.data.brand.name").value("Modified Brand"));

            verify(productFacade).modifyBrand(eq(BRAND_ID), any(BrandModifyRequestDto.class));
        }

        @Test
        @DisplayName("존재하지 않는 브랜드 수정 시 404 Not Found를 반환한다")
        @WithMockUser
        void modifyBrand_Fail_BrandNotFound() throws Exception {
            // given
            BrandModifyRequestDto requestDto = BrandModifyRequestDto.builder()
                    .name("NonExistent Brand")
                    .imageUrl("https://example.com/non_existent.png")
                    .build();

            given(productFacade.modifyBrand(eq(BRAND_ID), any(BrandModifyRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.BRAND_NOT_FOUND));

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("BRAND_NOT_FOUND"));

            verify(productFacade).modifyBrand(eq(BRAND_ID), any(BrandModifyRequestDto.class));
        }

        @Test
        @DisplayName("중복된 이름으로 브랜드 수정 시 409 Conflict를 반환한다")
        @WithMockUser
        void modifyBrand_Fail_DuplicateName() throws Exception {
            // given
            BrandModifyRequestDto requestDto = BrandModifyRequestDto.builder()
                    .name("Existing Brand Name")
                    .imageUrl("https://example.com/existing.png")
                    .build();

            given(productFacade.modifyBrand(eq(BRAND_ID), any(BrandModifyRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.BRAND_NAME_DUPLICATE));

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("BRAND_NAME_DUPLICATE"));

            verify(productFacade).modifyBrand(eq(BRAND_ID), any(BrandModifyRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 브랜드 수정 시 400 Bad Request를 반환한다")
        @WithMockUser
        void modifyBrand_Fail_Validation() throws Exception {
            // given
            // Invalid name (blank) and invalid URL
            BrandModifyRequestDto requestDto = BrandModifyRequestDto.builder()
                    .name(" ")
                    .imageUrl("invalid-url")
                    .build();

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).modifyBrand(eq(BRAND_ID), any(BrandModifyRequestDto.class));
        }
    }
}