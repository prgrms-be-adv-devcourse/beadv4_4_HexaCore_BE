package com.back.image.app;

import com.back.image.app.usecase.ConvertImageUseCase;
import com.back.image.app.usecase.FlushImageUseCase;
import com.back.image.app.usecase.ResizeImageUseCase;
import com.back.image.app.usecase.UploadImageUseCase;
import com.back.image.dto.enums.ImageCategory;
import com.back.image.dto.response.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageFacade {
    private final ConvertImageUseCase convertImageUseCase;
    private final ResizeImageUseCase resizeImageUseCase;
    private final UploadImageUseCase uploadImageUseCase;
    private final FlushImageUseCase flushImageUseCase;

    public ImageUploadResponseDto uploadImage(List<MultipartFile> images, ImageCategory category) {
        List<File> allTemporaryFiles = new ArrayList<>();

        try {
            List<File> convertedImages = convertImageUseCase.convertMultipleFile(images);
            allTemporaryFiles.addAll(convertedImages);

            List<File> resizeFiles = resizeImageUseCase.resizeMultipleImage(convertedImages);
            allTemporaryFiles.addAll(resizeFiles);

            List<String> uploadedImageUrls = uploadImageUseCase.uploadMultipleImage(resizeFiles, category.getPath());

            return ImageUploadResponseDto.builder().fileUrl(uploadedImageUrls).build();
        } finally {
            if (!allTemporaryFiles.isEmpty()) {
                log.info("[FileCleanup] 임시 파일 정리 시작, 파일 개수: {}", allTemporaryFiles.size());
                flushImageUseCase.flushMultipleFile(allTemporaryFiles);
            }
        }
    }
}
