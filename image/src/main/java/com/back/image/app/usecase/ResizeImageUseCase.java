package com.back.image.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.image.config.ImageResizeProperties;
import com.back.image.utils.ImageUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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

            BufferedImage originalImage = ImageIO.read(convertedImage);
            if (originalImage == null) {
                log.error("[ImageProcessingFailed] 이미지 파일을 읽을 수 없습니다: {}", convertedImage.getName());
                throw new CustomException("[ImageProcessingFailed] 이미지 파일을 읽을 수 없습니다: " + convertedImage.getName(), FailureCode.IMAGE_PROCESSING_FAILED);
            }

            int originWidth = originalImage.getWidth();
            int originHeight = originalImage.getHeight();

            if (isResizeUnnecessary(originWidth, originHeight)) {
                return convertedImage;
            }

            BufferedImage resizedImage = handleResizeImage(originalImage, originWidth, originHeight);

            return buildImageFile(resizedImage, convertedImage);
        } catch (IOException e) {
            log.error("[ImageProcessingFailed] 이미지 리사이징 중 IOException 발생: {}", e.getMessage(), e);
            throw new CustomException("[ImageProcessingFailed] 이미지 리사이지 중 IOException 발생 : " + e.getMessage(), FailureCode.IMAGE_PROCESSING_FAILED);
        }
    }

    private boolean isResizeUnnecessary(int originWidth, int originHeight) {
        return originWidth <= imageResizeProperties.getWidth() && originHeight <= imageResizeProperties.getHeight();
    }

    private BufferedImage handleResizeImage(BufferedImage originalImage, int originWidth, int originHeight) {
        double widthScale = (double) imageResizeProperties.getWidth() / (double) originWidth;
        double heightScale = (double) imageResizeProperties.getHeight() / (double) originHeight;
        double scale = Math.min(widthScale, heightScale);

        int resizedWidth = (int) Math.round(originWidth * scale);
        int resizedHeight = (int) Math.round(originHeight * scale);

        Image scaledImage = originalImage.getScaledInstance(resizedWidth, resizedHeight, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(resizedWidth, resizedHeight, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        return resizedImage;
    }

    private File buildImageFile(BufferedImage resizedImage, File convertedImage) throws IOException {
        File resizedFile = new File(System.getProperty("java.io.tmpdir") + "/resized_" + convertedImage.getName());

        File parentDir = resizedFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!imageUtility.validateFileExtension(resizedFile.getName())) {
            log.error("[ImageProcessingFailed] 지원하지 않는 파일 형식입니다: {}", resizedFile.getName());
            throw new IOException("[ImageProcessingFailed] 지원하지 않는 파일 형식입니다: " + resizedFile.getName());
        }

        String fileExtension = imageUtility.getFileExtension(resizedFile.getName());

        ImageIO.write(resizedImage, fileExtension, resizedFile);

        if (!resizedFile.exists()) {
            log.error("[ImageProcessingFailed] 리사이즈된 파일 생성에 실패했습니다.");
            throw new IOException("[ImageProcessingFailed] 리사이즈된 파일 생성에 실패했습니다.");
        }

        return resizedFile;
    }
}
