package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.product.app.ProductFacade;
import com.back.product.dto.OptionDto;
import com.back.product.dto.request.OptionAppendRequestDto;
import com.back.product.dto.request.OptionCreateRequestDto;
import com.back.product.dto.request.OptionGroupModifyRequestDto;
import com.back.product.dto.response.OptionGroupModifyResponseDto;
import com.back.product.dto.response.OptionListResponseDto;
import com.back.product.dto.response.OptionResponseDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiV1OptionController.class)
@DisplayName("ApiV1OptionController 테스트")
@ActiveProfiles("test")
public class ApiV1OptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private ProductFacade productFacade;
    
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setupObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("GET /api/v1/products/options")
    class GetOptionsTest {
        @Test
        @DisplayName("상품 옵션 목록 조회 컨트롤러 단위 테스트")
        void getOptions_unit_test() throws Exception {
            // given: 테스트 준비
            OptionDto.GroupDto color = OptionDto.GroupDto.builder().id(1L).name("색상").build();
            List<OptionDto.ValueDto> colorValues = List.of(
                    OptionDto.ValueDto.builder().id(1L).name("빨강").build(),
                    OptionDto.ValueDto.builder().id(2L).name("파랑").build()
            );
            OptionDto.GroupDto size = OptionDto.GroupDto.builder().id(2L).name("사이즈").build();
            List<OptionDto.ValueDto> sizeValues = List.of(
                    OptionDto.ValueDto.builder().id(1L).name("L").build(),
                    OptionDto.ValueDto.builder().id(2L).name("M").build()
            );
            OptionListResponseDto response = OptionListResponseDto.builder()
                    .options(List.of(
                            OptionDto.builder().group(color).values(colorValues).build(),
                            OptionDto.builder().group(size).values(sizeValues).build()
                    ))
                    .build();

            given(productFacade.getOptions()).willReturn(response);

            mockMvc.perform(
                            get("/api/v1/products/options")
                                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productFacade).getOptions();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products/options")
    class CreateOptionsTest {

        @Test
        @DisplayName("상품 옵션 생성 컨트롤러 단위 테스트 - 성공")
        void createOptions_unit_test_success() throws Exception {
            // given: 테스트 준비
            // 1. 요청 DTO 생성
            OptionCreateRequestDto.OptionDto requestOption1 = OptionCreateRequestDto.OptionDto.builder()
                    .group("color")
                    .values(List.of("red", "blue"))
                    .build();
            OptionCreateRequestDto.OptionDto requestOption2 = OptionCreateRequestDto.OptionDto.builder()
                    .group("size")
                    .values(List.of("small", "large"))
                    .build();
            OptionCreateRequestDto requestDto = OptionCreateRequestDto.builder()
                    .options(List.of(requestOption1, requestOption2))
                    .build();

            // 2. 응답 DTO 생성
            OptionDto.GroupDto responseGroup1 = OptionDto.GroupDto.builder().id(1L).name("color").build();
            List<OptionDto.ValueDto> responseValues1 = List.of(
                    OptionDto.ValueDto.builder().id(101L).name("red").build(),
                    OptionDto.ValueDto.builder().id(102L).name("blue").build()
            );
            OptionDto.GroupDto responseGroup2 = OptionDto.GroupDto.builder().id(2L).name("size").build();
            List<OptionDto.ValueDto> responseValues2 = List.of(
                    OptionDto.ValueDto.builder().id(201L).name("small").build(),
                    OptionDto.ValueDto.builder().id(202L).name("large").build()
            );
            OptionListResponseDto expectedResponseDto = OptionListResponseDto.builder()
                    .options(List.of(
                            OptionDto.builder().group(responseGroup1).values(responseValues1).build(),
                            OptionDto.builder().group(responseGroup2).values(responseValues2).build()
                    ))
                    .build();

            // 3. ProductFacade 모킹
            given(productFacade.createOptions(any(OptionCreateRequestDto.class))).willReturn(expectedResponseDto);

            // when & then: 컨트롤러 실행 및 결과 검증
            mockMvc.perform(post("/api/v1/products/options")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isCreated()) // 201 Created
                    .andExpect(jsonPath("$.code").value(SuccessCode.CREATED.name()))
                    .andExpect(jsonPath("$.message").value(SuccessCode.CREATED.getMessage()))
                    .andExpect(jsonPath("$.data.options[0].group.name").value("color"))
                    .andExpect(jsonPath("$.data.options[0].values[0].name").value("red"))
                    .andExpect(jsonPath("$.data.options[1].group.name").value("size"))
                    .andExpect(jsonPath("$.data.options[1].values[1].name").value("large"));

            // ProductFacade의 createOptions 메서드가 올바른 인수로 한 번 호출되었는지 검증
            verify(productFacade).createOptions(any(OptionCreateRequestDto.class));
        }

        @Test
        @DisplayName("상품 옵션 생성 컨트롤러 단위 테스트 - 유효성 검사 실패 (빈 Options 리스트)")
        void createOptions_unit_test_validation_failure_empty_options() throws Exception {
            // given: 유효하지 않은 요청 DTO 생성 (options 리스트가 비어 있음)
            OptionCreateRequestDto requestDto = OptionCreateRequestDto.builder()
                    .options(List.of()) // Empty list
                    .build();

            // when & then: 컨트롤러 실행 및 결과 검증
            mockMvc.perform(post("/api/v1/products/options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 400 Bad Request

            // ProductFacade의 createOptions 메서드가 호출되지 않았는지 검증
            verify(productFacade, never()).createOptions(any(OptionCreateRequestDto.class));
        }

        @Test
        @DisplayName("상품 옵션 생성 컨트롤러 단위 테스트 - 유효성 검사 실패 (Option Group Name 공백)")
        void createOptions_unit_test_validation_failure_blank_group_name() throws Exception {
            // given: 유효하지 않은 요청 DTO 생성 (group 이름이 공백)
            OptionCreateRequestDto.OptionDto requestOption = OptionCreateRequestDto.OptionDto.builder()
                    .group("  ") // Blank group name
                    .values(List.of("value1"))
                    .build();
            OptionCreateRequestDto requestDto = OptionCreateRequestDto.builder()
                    .options(List.of(requestOption))
                    .build();

            // when & then: 컨트롤러 실행 및 결과 검증
            mockMvc.perform(post("/api/v1/products/options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 400 Bad Request

            // ProductFacade의 createOptions 메서드가 호출되지 않았는지 검증
            verify(productFacade, never()).createOptions(any(OptionCreateRequestDto.class));
        }

        @Test
        @DisplayName("상품 옵션 생성 컨트롤러 단위 테스트 - 유효성 검사 실패 (Option Values 리스트 공백)")
        void createOptions_unit_test_validation_failure_empty_values() throws Exception {
            // given: 유효하지 않은 요청 DTO 생성 (values 리스트가 비어 있음)
            OptionCreateRequestDto.OptionDto requestOption = OptionCreateRequestDto.OptionDto.builder()
                    .group("group1")
                    .values(List.of()) // Empty values list
                    .build();
            OptionCreateRequestDto requestDto = OptionCreateRequestDto.builder()
                    .options(List.of(requestOption))
                    .build();

            // when & then: 컨트롤러 실행 및 결과 검증
            mockMvc.perform(post("/api/v1/products/options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()); // 400 Bad Request

            // ProductFacade의 createOptions 메서드가 호출되지 않았는지 검증
            verify(productFacade, never()).createOptions(any(OptionCreateRequestDto.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products/options/{optionGroupId}")
    class AppendOptionsTest {

        @Test
        @DisplayName("상품 옵션 값 추가 컨트롤러 단위 테스트 - 성공")
        void appendOptions_success() throws Exception {
            // given
            Long optionGroupId = 1L;
            OptionAppendRequestDto requestDto = OptionAppendRequestDto.builder()
                    .values(List.of("newValue1", "newValue2"))
                    .build();

            OptionDto.GroupDto responseGroup = OptionDto.GroupDto.builder().id(optionGroupId).name("color").build();
            List<OptionDto.ValueDto> responseValues = List.of(
                    OptionDto.ValueDto.builder().id(1L).name("oldValue").build(),
                    OptionDto.ValueDto.builder().id(2L).name("newValue1").build(),
                    OptionDto.ValueDto.builder().id(3L).name("newValue2").build()
            );
            OptionResponseDto expectedResponseDto = OptionResponseDto.builder()
                    .option(OptionDto.builder().group(responseGroup).values(responseValues).build())
                    .build();

            given(productFacade.appendOptions(anyLong(), any(OptionAppendRequestDto.class))).willReturn(expectedResponseDto);

            // when & then
            mockMvc.perform(post("/api/v1/products/options/{optionGroupId}", optionGroupId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(SuccessCode.CREATED.name()))
                    .andExpect(jsonPath("$.data.option.group.name").value("color"))
                    .andExpect(jsonPath("$.data.option.values.length()").value(3));

            verify(productFacade).appendOptions(eq(optionGroupId), any(OptionAppendRequestDto.class));
        }

        @Test
        @DisplayName("상품 옵션 값 추가 컨트롤러 단위 테스트 - 유효성 검사 실패 (빈 values 리스트)")
        void appendOptions_validationFailure_emptyList() throws Exception {
            // given
            Long optionGroupId = 1L;
            OptionAppendRequestDto requestDto = OptionAppendRequestDto.builder()
                    .values(List.of())
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/products/options/{optionGroupId}", optionGroupId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).appendOptions(anyLong(), any(OptionAppendRequestDto.class));
        }

        @Test
        @DisplayName("상품 옵션 값 추가 컨트롤러 단위 테스트 - 유효하지 않은 패턴)")
        void appendOptions_validationFailure_invalidPattern() throws Exception {
            // given
            Long optionGroupId = 1L;
            OptionAppendRequestDto requestDto = OptionAppendRequestDto.builder()
                    .values(List.of("invalid!value"))
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/products/options/{optionGroupId}", optionGroupId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).appendOptions(anyLong(), any(OptionAppendRequestDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/options/groups/{optionGroupId}")
    class ModifyOptionGroupTest {

        @Test
        @DisplayName("옵션 그룹 이름 수정 컨트롤러 단위 테스트 - 성공")
        void modifyOptionGroup_success() throws Exception {
            // given
            Long optionGroupId = 1L;
            String newGroupName = "newcolor";
            OptionGroupModifyRequestDto requestDto = OptionGroupModifyRequestDto.builder()
                    .name(newGroupName)
                    .build();

            OptionGroupModifyResponseDto expectedResponseDto = OptionGroupModifyResponseDto.builder()
                    .id(optionGroupId)
                    .name(newGroupName)
                    .updatedAt(LocalDateTime.now())
                    .build();

            given(productFacade.modifyOptionGroup(eq(optionGroupId), any(OptionGroupModifyRequestDto.class))).willReturn(expectedResponseDto);

            // when & then
            mockMvc.perform(put("/api/v1/products/options/groups/{optionGroupId}", optionGroupId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(SuccessCode.OK.name()))
                    .andExpect(jsonPath("$.message").value(SuccessCode.OK.getMessage()))
                    .andExpect(jsonPath("$.data.id").value(optionGroupId))
                    .andExpect(jsonPath("$.data.name").value(newGroupName));

            verify(productFacade).modifyOptionGroup(eq(optionGroupId), any(OptionGroupModifyRequestDto.class));
        }

        @Test
        @DisplayName("옵션 그룹 이름 수정 컨트롤러 단위 테스트 - 유효성 검사 실패 (공백 이름)")
        void modifyOptionGroup_validationFailure_blankName() throws Exception {
            // given
            Long optionGroupId = 1L;
            OptionGroupModifyRequestDto requestDto = OptionGroupModifyRequestDto.builder()
                    .name("  ") // Blank name
                    .build();

            // when & then
            mockMvc.perform(put("/api/v1/products/options/groups/{optionGroupId}", optionGroupId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).modifyOptionGroup(anyLong(), any(OptionGroupModifyRequestDto.class));
        }

        @Test
        @DisplayName("옵션 그룹 이름 수정 컨트롤러 단위 테스트 - 유효성 검사 실패 (유효하지 않은 패턴)")
        void modifyOptionGroup_validationFailure_invalidPattern() throws Exception {
            // given
            Long optionGroupId = 1L;
            OptionGroupModifyRequestDto requestDto = OptionGroupModifyRequestDto.builder()
                    .name("InvalidName123") // Invalid pattern (uppercase, numbers)
                    .build();

            // when & then
            mockMvc.perform(put("/api/v1/products/options/groups/{optionGroupId}", optionGroupId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productFacade, never()).modifyOptionGroup(anyLong(), any(OptionGroupModifyRequestDto.class));
        }
    }
}