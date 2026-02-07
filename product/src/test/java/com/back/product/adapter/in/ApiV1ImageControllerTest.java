package com.back.product.adapter.in;

import com.back.image.app.ImageFacade;
import com.back.common.code.SuccessCode;
import com.back.image.dto.enums.ImageCategory;
import com.back.image.dto.response.ImageUploadResponseDto;
import com.back.security.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiV1ImageController.class)
class ApiV1ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private ImageFacade imageFacade;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /api/v1/images/upload/{category}")
    class GetCategoriesTest {
        @DisplayName("이미지 업로드 성공")
        @Test
        void uploadImages_success() throws Exception {
            // given
            ImageCategory category = ImageCategory.PRODUCT;
            String imageUrl = "http://example.com/image.jpg";
            ImageUploadResponseDto mockResponseDto = new ImageUploadResponseDto(List.of(imageUrl));

            MockMultipartFile mockFile = new MockMultipartFile(
                    "images",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes(StandardCharsets.UTF_8)
            );

            when(imageFacade.uploadImage(any(List.class), any(ImageCategory.class)))
                    .thenReturn(mockResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(multipart("/api/v1/images/upload/{category}", category.name())
                            .file(mockFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .accept(MediaType.APPLICATION_JSON)
                    )
                    .andDo(print());

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(SuccessCode.OK.getCode()))
                    .andExpect(jsonPath("$.message").value(SuccessCode.OK.getMessage()))
                    .andExpect(jsonPath("$.data.fileUrl[0]").value(imageUrl));
        }

        @DisplayName("이미지 업로드 실패 - 유효하지 않은 카테고리")
        @Test
        void uploadImages_invalidCategory() throws Exception {
            // given
            String invalidCategory = "INVALID_CATEGORY";

            MockMultipartFile mockFile = new MockMultipartFile(
                    "images",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes(StandardCharsets.UTF_8)
            );

            // when
            ResultActions resultActions = mockMvc.perform(multipart("/api/v1/images/upload/{category}", invalidCategory)
                            .file(mockFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .accept(MediaType.APPLICATION_JSON)
                    )
                    .andDo(print());

            // then
            resultActions.andExpect(status().isBadRequest());
        }

        @DisplayName("이미지 업로드 실패 - 파일 없이 요청")
        @Test
        void uploadImages_noFile() throws Exception {
            // given
            ImageCategory category = ImageCategory.PRODUCT;

            // when
            ResultActions resultActions = mockMvc.perform(multipart("/api/v1/images/upload/{category}", category.name())
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE) // Even without file, need to set content type
                            .accept(MediaType.APPLICATION_JSON)
                    )
                    .andDo(print());

            // then
            // Expecting a 400 Bad Request if @RequestPart List<MultipartFile> images is empty or missing
            resultActions.andExpect(status().isBadRequest());
        }
    }
}
