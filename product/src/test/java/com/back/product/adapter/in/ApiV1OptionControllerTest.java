package com.back.product.adapter.in;

import com.back.product.app.ProductFacade;
import com.back.product.app.usecase.OptionUseCase;
import com.back.product.dto.OptionDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.security.jwt.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiV1OptionController.class)
@DisplayName("ApiV1OptionController 테스트")
public class ApiV1OptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private OptionUseCase optionUseCase;

    @MockitoBean
    private ProductFacade productFacade;

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
            OptionResponseDto response = OptionResponseDto.builder()
                    .options(List.of(
                            OptionDto.builder().group(color).values(colorValues).build(),
                            OptionDto.builder().group(size).values(sizeValues).build()
                    ))
                    .build();

            given(optionUseCase.findAllOptions()).willReturn(response);

            mockMvc.perform(
                            get("/api/v1/products/options")
                                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productFacade).getOptions();
        }
    }
}
