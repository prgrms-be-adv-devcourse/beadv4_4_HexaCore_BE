package com.back.image.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.image.config.ImageResizeProperties;
import com.back.image.utils.ImageUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResizeImageUseCase {
    private final ImageUtility imageUtility;
    private final ImageResizeProperties imageResizeProperties;

    @Loggable(logArgs = false)
    public List<File> resizeMultipleImage(List<File> convertedImages) {
        return convertedImages.stream().map(f -> {
            try {
                return resizeImage(f);
            } catch (CustomException e) {
                log.error("[ImageProcessingFailed] 이미지 리사이징 실패 : {}", e.getMessage(), e);
                throw e;
            }
        }).toList();
    }

    @Loggable(logArgs = false)
    public File resizeImage(File convertedImage) {
        try {
            if (convertedImage == null || !convertedImage.exists()) {
                log.error("[ImageProcessingFailed] 리사이즈할 파일이 존재하지 않습니다.");
                throw new CustomException("[ImageProcessingFailed] 리사이즈할 파일이 존재하지 않습니다.", FailureCode.IMAGE_PROCESSING_FAILED);
            }

            File resizedImage = new File(System.getProperty("java.io.tmpdir") + "/resized_" + convertedImage.getName());

            Thumbnails.of(convertedImage)
                    .size(imageResizeProperties.getWidth(), imageResizeProperties.getHeight())
                    .rendering(Rendering.QUALITY)
                    .antialiasing(Antialiasing.ON)
                    .outputQuality(0.85)
                    .toFile(resizedImage);

            return resizedImage;
        } catch (IOException e) {
            log.error("[ImageProcessingFailed] 이미지 리사이징 중 IOException 발생: {}", e.getMessage(), e);
            throw new CustomException("[ImageProcessingFailed] 이미지 리사이지 중 IOException 발생 : " + e.getMessage(), FailureCode.IMAGE_PROCESSING_FAILED);
        }
    }
}
