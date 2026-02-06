package com.back.app;

import com.back.app.usecase.ConvertImageUseCase;
import com.back.app.usecase.FlushImageUseCase;
import com.back.app.usecase.ResizeImageUseCase;
import com.back.app.usecase.UploadImageUseCase;
import com.back.dto.request.ImageUploadRequestDto;
import com.back.dto.response.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public ImageUploadResponseDto uploadImage(ImageUploadRequestDto imageUploadRequestDto) {
        List<File> allTemporaryFiles = new ArrayList<>();

        try {
            List<File> convertedImages = convertImageUseCase.convertMultipleFile(imageUploadRequestDto.files());
            allTemporaryFiles.addAll(convertedImages);

            List<File> resizeFiles = resizeImageUseCase.resizeMultipleImage(convertedImages);
            allTemporaryFiles.addAll(resizeFiles);

            // List<String> uploadedImageUrls = uploadImageUseCase.uploadMultipleImage(resizeFiles);

            return ImageUploadResponseDto.builder().build();
        } finally {
            if (!allTemporaryFiles.isEmpty()) {
                log.info("[FileCleanup] 임시 파일 정리 시작, 파일 개수: {}", allTemporaryFiles.size());
                flushImageUseCase.flushMultipleFile(allTemporaryFiles);
            }
        }
    }
}
