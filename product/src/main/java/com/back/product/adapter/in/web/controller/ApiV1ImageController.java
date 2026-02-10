package com.back.product.adapter.in.web.controller;

import com.back.image.app.ImageFacade;
import com.back.image.dto.enums.ImageCategory;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.image.dto.response.ImageUploadResponseDto;
import com.back.product.adapter.in.web.api.ImageApiController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/products/images")
@RequiredArgsConstructor
public class ApiV1ImageController implements ImageApiController {
    private final ImageFacade imageFacade;

    @Override
    @PostMapping(path = "/upload/{category}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResponse<ImageUploadResponseDto> uploadImages(
            @PathVariable ImageCategory category,
            @RequestPart List<MultipartFile> images
    ) {
        ImageUploadResponseDto response = imageFacade.uploadImage(images, category);
        return CommonResponse.success(SuccessCode.OK, response);
    }
}
