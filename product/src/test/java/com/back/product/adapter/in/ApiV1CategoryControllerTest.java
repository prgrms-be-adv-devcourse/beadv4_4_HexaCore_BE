package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.ProductFacade;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.request.CategoryCreateRequestDto;
import com.back.product.dto.response.CategoryListResponseDto;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiV1CategoryController.class)
@DisplayName("ApiV1CategoryController 테스트")
class ApiV1CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private ProductFacade productFacade;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /api/v1/products/categories")
    class GetCategoriesTest {
        @Test
        @DisplayName("카테고리 목록 조회를 성공한다")
        @WithMockUser
        void getCategories() throws Exception {
            // given
            given(productFacade.getCategories()).willReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(
                            get("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                    ).andDo(print())
                    .andExpect(status().isOk());

            verify(productFacade).getCategories();
        }
    }


    @Nested
    @DisplayName("POST /api/v1/products/categories")
    class CreateCategoriesTest {

        @Test
        @DisplayName("카테고리 생성을 성공한다")
        @WithMockUser
        void createCategories_Success() throws Exception {
            // given
            CategoryCreateRequestDto.CategoryDto newCategory1 = new CategoryCreateRequestDto.CategoryDto("Tops", "https://example.com/image1.png");
            CategoryCreateRequestDto.CategoryDto newCategory2 = new CategoryCreateRequestDto.CategoryDto("Bottoms", "https://example.com/image2.png");
            CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto(List.of(newCategory1, newCategory2));

            CategoryListResponseDto responseDto = CategoryListResponseDto.builder()
                    .categories(List.of(
                            new CategoryDto(1L, "Tops", "https://example.com/image1.png"),
                            new CategoryDto(2L, "Bottoms", "https://example.com/image2.png")
                    )).build();

            given(productFacade.createCategories(any(CategoryCreateRequestDto.class))).willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("CREATED"))
                    .andExpect(jsonPath("$.data.categories.length()").value(2))
                    .andExpect(jsonPath("$.data.categories[0].name").value("Tops"))
                    .andExpect(jsonPath("$.data.categories[1].name").value("Bottoms"));

            verify(productFacade).createCategories(any(CategoryCreateRequestDto.class));
        }

        @Test
        @DisplayName("요청에 중복된 카테고리 이름이 있어도, 새로운 카테고리만 생성하고 201 Created를 반환한다")
        @WithMockUser
        void createCategories_Filter_DuplicateName() throws Exception {
            // given
            CategoryCreateRequestDto.CategoryDto existingCategory = new CategoryCreateRequestDto.CategoryDto("Existed", "https://example.com/image_exist.png");
            CategoryCreateRequestDto.CategoryDto newCategory = new CategoryCreateRequestDto.CategoryDto("New", "https://example.com/image_new.png");
            CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto(List.of(existingCategory, newCategory));

            CategoryListResponseDto responseDto = CategoryListResponseDto.builder()
                    .categories(List.of(
                            new CategoryDto(1L, "New", "https://example.com/image_new.png")
                    )).build();

            given(productFacade.createCategories(any(CategoryCreateRequestDto.class)))
                    .willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.categories.length()").value(1))
                    .andExpect(jsonPath("$.data.categories[0].name").value("New"));

            verify(productFacade).createCategories(any(CategoryCreateRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 생성 시 400 Bad Request를 반환한다")
        @WithMockUser
        void createCategories_Fail_Validation() throws Exception {
            // given
            CategoryCreateRequestDto.CategoryDto invalidCategory = new CategoryCreateRequestDto.CategoryDto("123", "invalid-url");
            CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto(List.of(invalidCategory));

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).createCategories(any(CategoryCreateRequestDto.class));
        }
    }
}
