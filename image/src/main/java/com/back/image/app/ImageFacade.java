package com.back.image.app;

import com.back.common.annotation.Loggable;
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
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageFacade {
    private final ConvertImageUseCase convertImageUseCase;
    private final ResizeImageUseCase resizeImageUseCase;
    private final UploadImageUseCase uploadImageUseCase;
    private final FlushImageUseCase flushImageUseCase;

    @Loggable(logArgs = false, logResult = false)
    public ImageUploadResponseDto uploadImage(List<MultipartFile> images, ImageCategory category) {
        // 병렬 처리를 위해 스레드 안전한 리스트 사용
        List<File> allTemporaryFiles = Collections.synchronizedList(new ArrayList<>());

        try {
            List<String> uploadedImageUrls = images.parallelStream()
                    .map(image -> {
                        // 1. 이미지 변환 (임시 파일 생성)
                        File originalFile = convertImageUseCase.convertFile(image);
                        allTemporaryFiles.add(originalFile);

                        // 2. 이미지 리사이징 (필요한 경우 새 임시 파일 생성)
                        File resizedFile = resizeImageUseCase.resizeImage(originalFile);
                        if (resizedFile != originalFile) {
                            allTemporaryFiles.add(resizedFile);
                        }

                        // 3. S3 업로드
                        return uploadImageUseCase.uploadImage(resizedFile, category.getPath());
                    })
                    .toList();

            return ImageUploadResponseDto.builder().fileUrl(uploadedImageUrls).build();
        } finally {
            if (!allTemporaryFiles.isEmpty()) {
                log.info("[FileCleanup] 임시 파일 정리 시작, 파일 개수: {}", allTemporaryFiles.size());
                flushImageUseCase.flushMultipleFile(new ArrayList<>(allTemporaryFiles));
            }
        }
    }
}
