package com.back.product.adapter.in;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.ProductFacade;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.request.CategoryCreateRequestDto;
import com.back.security.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

@WebMvcTest(ApiV1CategoryController.class)
@DisplayName("ApiV1CategoryController 테스트")
class ApiV1CategoryControllerTest {

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
    class CreateCategoryTest {

        @Test
        @DisplayName("카테고리 생성을 성공한다")
        @WithMockUser
        void createCategory_Success() throws Exception {
            // given
            CategoryCreateRequestDto requestDto = CategoryCreateRequestDto.builder().name("Tops").imageUrl("image.png").build();
            CategoryDto responseDto = CategoryDto.builder().name("Tops").build();

            given(productFacade.createCategory(any(CategoryCreateRequestDto.class))).willReturn(responseDto);

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("CREATED"))
                    .andExpect(jsonPath("$.data.category.name").value("Tops"));

            verify(productFacade).createCategory(any(CategoryCreateRequestDto.class));
        }

        @Test
        @DisplayName("중복된 카테고리 이름으로 생성 시 409 Conflict를 반환한다")
        @WithMockUser
        void createCategory_Fail_DuplicateName() throws Exception {
            // given
            CategoryCreateRequestDto requestDto = CategoryCreateRequestDto.builder().name("Existed").imageUrl("image.png").build();
            given(productFacade.createCategory(any(CategoryCreateRequestDto.class)))
                    .willThrow(new CustomException(FailureCode.CATEGORY_NAME_DUPLICATE));

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isConflict());

            verify(productFacade).createCategory(any(CategoryCreateRequestDto.class));
        }

        @Test
        @DisplayName("유효하지 않은 요청 값으로 생성 시 400 Bad Request를 반환한다")
        @WithMockUser
        void createCategory_Fail_Validation() throws Exception {
            // given
            CategoryCreateRequestDto requestDto = CategoryCreateRequestDto.builder().name(" ").name("image.png").build();

            // when & then
            mockMvc.perform(
                            post("/api/v1/products/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDto))
                    ).andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).createCategory(any(CategoryCreateRequestDto.class));
        }
    }
}
