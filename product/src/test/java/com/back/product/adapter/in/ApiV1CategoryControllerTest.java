package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.in.web.controller.ApiV1CategoryController;
import com.back.product.app.facade.ProductFacade;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.request.CategoryListCreateRequestDto;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.dto.response.CategoryListResponseDto;
import com.back.product.dto.response.CategoryResponseDto;
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
            CategoryDataRequestDto newCategory1 = new CategoryDataRequestDto("Tops", "https://example.com/image1.png");
            CategoryDataRequestDto newCategory2 = new CategoryDataRequestDto("Bottoms", "https://example.com/image2.png");
            CategoryListCreateRequestDto requestDto = new CategoryListCreateRequestDto(List.of(newCategory1, newCategory2));

            CategoryListResponseDto responseDto = CategoryListResponseDto.builder()
                    .categories(List.of(
                            new CategoryDto(1L, "Tops", "https://example.com/image1.png"),
                            new CategoryDto(2L, "Bottoms", "https://example.com/image2.png")
                    )).build();

            given(productFacade.createCategories(any(CategoryListCreateRequestDto.class))).willReturn(responseDto);

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

            verify(productFacade).createCategories(any(CategoryListCreateRequestDto.class));
        }

        @Test
        @DisplayName("요청에 중복된 카테고리 이름이 있어도, 새로운 카테고리만 생성하고 201 Created를 반환한다")
        @WithMockUser
        void createCategories_Filter_DuplicateName() throws Exception {
            // given
            CategoryDataRequestDto existingCategory = new CategoryDataRequestDto("Existed", "https://example.com/image_exist.png");
            CategoryDataRequestDto newCategory = new CategoryDataRequestDto("New", "https://example.com/image_new.png");
            CategoryListCreateRequestDto requestDto = new CategoryListCreateRequestDto(List.of(existingCategory, newCategory));

            CategoryListResponseDto responseDto = CategoryListResponseDto.builder()
                    .categories(List.of(
                            new CategoryDto(1L, "New", "https://example.com/image_new.png")
                    )).build();

            given(productFacade.createCategories(any(CategoryListCreateRequestDto.class)))
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

            verify(productFacade).createCategories(any(CategoryListCreateRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 생성 시 400 Bad Request를 반환한다")
        @WithMockUser
        void createCategories_Fail_Validation() throws Exception {
            // given
            CategoryDataRequestDto invalidCategory = new CategoryDataRequestDto("123", "invalid-url");
            CategoryListCreateRequestDto requestDto = new CategoryListCreateRequestDto(List.of(invalidCategory));

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).createCategories(any(CategoryListCreateRequestDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/categories/{categoryId}")
    class ModifyCategoryTest {

        private final Long CATEGORY_ID = 1L;

        @Test
        @DisplayName("카테고리 수정을 성공한다")
        @WithMockUser
        void modifyCategory_Success() throws Exception {
            // given
            CategoryDataRequestDto requestDto = CategoryDataRequestDto.builder()
                    .name("ModifiedCategory")
                    .imageUrl("https://example.com/modified_image.png")
                    .build();
            CategoryResponseDto responseDto = CategoryResponseDto.builder()
                    .category(new CategoryDto(CATEGORY_ID, "Modified Category", "https://example.com/modified_image.png"))
                    .build();

            given(productFacade.modifyCategory(eq(CATEGORY_ID), any(CategoryDataRequestDto.class))).willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/categories/{categoryId}", CATEGORY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("OK"))
                    .andExpect(jsonPath("$.data.category.categoryId").value(CATEGORY_ID))
                    .andExpect(jsonPath("$.data.category.name").value("Modified Category"));

            verify(productFacade).modifyCategory(eq(CATEGORY_ID), any(CategoryDataRequestDto.class));
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 수정 시 404 Not Found를 반환한다")
        @WithMockUser
        void modifyCategory_Fail_CategoryNotFound() throws Exception {
            // given
            CategoryDataRequestDto requestDto = CategoryDataRequestDto.builder()
                    .name("NonExistentCategory")
                    .imageUrl("https://example.com/non_existent.png")
                    .build();

            given(productFacade.modifyCategory(eq(CATEGORY_ID), any(CategoryDataRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.CATEGORY_NOT_FOUND));

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/categories/{categoryId}", CATEGORY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));

            verify(productFacade).modifyCategory(eq(CATEGORY_ID), any(CategoryDataRequestDto.class));
        }

        @Test
        @DisplayName("중복된 이름으로 카테고리 수정 시 409 Conflict를 반환한다")
        @WithMockUser
        void modifyCategory_Fail_DuplicateName() throws Exception {
            // given
            CategoryDataRequestDto requestDto = CategoryDataRequestDto.builder()
                    .name("ExistingCategoryName")
                    .imageUrl("https://example.com/existing.png")
                    .build();

            given(productFacade.modifyCategory(eq(CATEGORY_ID), any(CategoryDataRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.CATEGORY_NAME_DUPLICATE));

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/categories/{categoryId}", CATEGORY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CATEGORY_NAME_DUPLICATE"));

            verify(productFacade).modifyCategory(eq(CATEGORY_ID), any(CategoryDataRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 카테고리 수정 시 400 Bad Request를 반환한다")
        @WithMockUser
        void modifyCategory_Fail_Validation() throws Exception {
            // given
            // Invalid name (blank or non-alphabet) and invalid URL
            CategoryDataRequestDto requestDto = CategoryDataRequestDto.builder()
                    .name("123") // Fails @Pattern(regexp = "^[a-zA-Z]+$")
                    .imageUrl("invalid-url")
                    .build();

            // when & then
            mockMvc.perform(
                            put("/api/v1/products/categories/{categoryId}", CATEGORY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).modifyCategory(eq(CATEGORY_ID), any(CategoryDataRequestDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/categories/{categoryId}")
    class DeleteCategoryTest {

        private final Long CATEGORY_ID = 1L;

        @Test
        @DisplayName("카테고리 삭제를 성공한다")
        @WithMockUser
        void deleteCategory_Success() throws Exception {
            // given
            // productFacade.deleteCategory(CATEGORY_ID)가 호출될 때 아무것도 하지 않도록 설정 (void 메소드)
            doNothing().when(productFacade).deleteCategory(CATEGORY_ID);

            // when & then
            mockMvc.perform(
                            delete("/api/v1/products/categories/{categoryId}", CATEGORY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                    ).andDo(print())
                    .andExpect(status().isNoContent());

            // productFacade.deleteCategory가 올바른 ID로 호출되었는지 검증
            verify(productFacade).deleteCategory(CATEGORY_ID);
        }

        @Test
        @DisplayName("사용 중인 카테고리를 삭제 시도 시 409 Conflict를 반환한다")
        @WithMockUser
        void deleteCategory_Fail_CategoryInUse() throws Exception {
            // given
            // productFacade.deleteCategory(CATEGORY_ID)가 호출될 때 CATEGORY_IN_USE 예외를 던지도록 설정
            doThrow(new CustomException(FailureCode.CATEGORY_IN_USE))
                    .when(productFacade).deleteCategory(CATEGORY_ID);

            // when & then
            mockMvc.perform(
                            delete("/api/v1/products/categories/{categoryId}", CATEGORY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                    ).andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CATEGORY_IN_USE"));

            // productFacade.deleteCategory가 올바른 ID로 호출되었는지 검증
            verify(productFacade).deleteCategory(CATEGORY_ID);
        }
    }
}
