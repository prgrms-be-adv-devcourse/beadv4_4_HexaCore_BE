package com.back.product.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.dto.enums.ImageCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Product", description = "상품 관련 API")
public interface ImageApiController {
    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일들을 업로드하여 S3에 저장합니다.")
    @ApiResponse(responseCode = "200", description = "이미지 파일 등록 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    CommonResponse<?> uploadImages(ImageCategory category, List<MultipartFile> images);
}
