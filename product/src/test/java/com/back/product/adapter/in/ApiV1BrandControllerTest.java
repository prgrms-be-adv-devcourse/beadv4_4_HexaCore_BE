package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.in.web.controller.ApiV1BrandController;
import com.back.product.app.facade.ProductFacade;
import com.back.product.dto.model.BrandDto;
import com.back.product.dto.request.BrandListCreateRequestDto;
import com.back.product.dto.request.BrandDataRequestDto;
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
import static org.mockito.Mockito.*;
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
            BrandDataRequestDto newBrand1 = new BrandDataRequestDto("New Balance", "https://example.com/logo.png");
            BrandDataRequestDto newBrand2 = new BrandDataRequestDto("Nike", "https://example.com/logo2.png");
            BrandListCreateRequestDto requestDto = new BrandListCreateRequestDto(List.of(newBrand1, newBrand2));

            BrandListResponseDto responseDto = BrandListResponseDto.builder()
                    .brands(List.of(
                            new BrandDto(1L, "New Balance", "https://example.com/logo.png"),
                            new BrandDto(2L, "Nike", "https://example.com/logo2.png")
                    )).build();

            given(productFacade.createBrands(any(BrandListCreateRequestDto.class))).willReturn(responseDto);

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

            verify(productFacade).createBrands(any(BrandListCreateRequestDto.class));
        }

        @Test
        @DisplayName("요청에 중복된 브랜드 이름이 포함되어 있어도, 생성 가능한 브랜드만 생성하고 201 Created를 반환한다")
        @WithMockUser
        void createBrand_Filter_DuplicateName() throws Exception {
            // given
            BrandDataRequestDto existingBrand = new BrandDataRequestDto("Existing Brand", "https://example.com/logo_exist.png");
            BrandDataRequestDto newBrand = new BrandDataRequestDto("New Brand", "https://example.com/logo_new.png");
            BrandListCreateRequestDto requestDto = new BrandListCreateRequestDto(List.of(existingBrand, newBrand));

            // UseCase에서 중복을 걸러내고, 새로 생성된 브랜드만 반환
            BrandListResponseDto responseDto = BrandListResponseDto.builder()
                    .brands(List.of(
                            new BrandDto(1L, "New Brand", "https://example.com/logo_new.png")
                    )).build();


            given(productFacade.createBrands(any(BrandListCreateRequestDto.class)))
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

            verify(productFacade).createBrands(any(BrandListCreateRequestDto.class));
        }


        @Test
        @DisplayName("유효하지 않은 요청 값으로 생성 시 400 Bad Request를 반환한다")
        @WithMockUser
        void createBrand_Fail_Validation() throws Exception {
            // given
            BrandDataRequestDto invalidBrand = new BrandDataRequestDto(" ", "invalid-url");
            BrandListCreateRequestDto requestDto = new BrandListCreateRequestDto(List.of(invalidBrand));


            // when & then
            mockMvc.perform(
                            post("/api/v1/products/brands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).createBrands(any(BrandListCreateRequestDto.class));
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
            BrandDataRequestDto requestDto = BrandDataRequestDto.builder()
                    .name("Modified Brand")
                    .imageUrl("https://example.com/modified_logo.png")
                    .build();
            BrandResponseDto responseDto = BrandResponseDto.builder()
                    .brand(new BrandDto(BRAND_ID, "Modified Brand", "https://example.com/modified_logo.png"))
                    .build();

            given(productFacade.modifyBrand(eq(BRAND_ID), any(BrandDataRequestDto.class))).willReturn(responseDto);

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

            verify(productFacade).modifyBrand(eq(BRAND_ID), any(BrandDataRequestDto.class));
        }

        @Test
        @DisplayName("존재하지 않는 브랜드 수정 시 404 Not Found를 반환한다")
        @WithMockUser
        void modifyBrand_Fail_BrandNotFound() throws Exception {
            // given
            BrandDataRequestDto requestDto = BrandDataRequestDto.builder()
                    .name("NonExistent Brand")
                    .imageUrl("https://example.com/non_existent.png")
                    .build();

            given(productFacade.modifyBrand(eq(BRAND_ID), any(BrandDataRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.BRAND_NOT_FOUND));

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("BRAND_NOT_FOUND"));

            verify(productFacade).modifyBrand(eq(BRAND_ID), any(BrandDataRequestDto.class));
        }

        @Test
        @DisplayName("중복된 이름으로 브랜드 수정 시 409 Conflict를 반환한다")
        @WithMockUser
        void modifyBrand_Fail_DuplicateName() throws Exception {
            // given
            BrandDataRequestDto requestDto = BrandDataRequestDto.builder()
                    .name("Existing Brand Name")
                    .imageUrl("https://example.com/existing.png")
                    .build();

            given(productFacade.modifyBrand(eq(BRAND_ID), any(BrandDataRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.BRAND_NAME_DUPLICATE));

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("BRAND_NAME_DUPLICATE"));

            verify(productFacade).modifyBrand(eq(BRAND_ID), any(BrandDataRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 브랜드 수정 시 400 Bad Request를 반환한다")
        @WithMockUser
        void modifyBrand_Fail_Validation() throws Exception {
            // given
            // Invalid name (blank) and invalid URL
            BrandDataRequestDto requestDto = BrandDataRequestDto.builder()
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

            verify(productFacade, never()).modifyBrand(eq(BRAND_ID), any(BrandDataRequestDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/brands/{brandId}")
    class DeleteBrandTest {

        private final Long BRAND_ID = 1L;

        @Test
        @DisplayName("브랜드 삭제를 성공한다")
        @WithMockUser
        void deleteBrand_Success() throws Exception {
            // given
            // productFacade.deleteBrand(BRAND_ID)가 호출될 때 아무것도 하지 않도록 설정 (void 메소드)
            doNothing().when(productFacade).deleteBrand(BRAND_ID);

            // when & then
            mockMvc.perform(
                            delete("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                    ).andDo(print())
                    .andExpect(status().isNoContent());

            // productFacade.deleteBrand가 올바른 ID로 호출되었는지 검증
            verify(productFacade).deleteBrand(BRAND_ID);
        }

        @Test
        @DisplayName("사용 중인 브랜드를 삭제 시도 시 409 Conflict를 반환한다")
        @WithMockUser
        void deleteBrand_Fail_BrandInUse() throws Exception {
            // given
            // productFacade.deleteBrand(BRAND_ID)가 호출될 때 BRAND_IN_USE 예외를 던지도록 설정
            doThrow(new CustomException(FailureCode.BRAND_IN_USE)).when(productFacade).deleteBrand(BRAND_ID);

            // when & then
            mockMvc.perform(
                            delete("/api/v1/products/brands/{brandId}", BRAND_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                    ).andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("BRAND_IN_USE"));

            // productFacade.deleteBrand가 올바른 ID로 호출되었는지 검증
            verify(productFacade).deleteBrand(BRAND_ID);
        }
    }
}