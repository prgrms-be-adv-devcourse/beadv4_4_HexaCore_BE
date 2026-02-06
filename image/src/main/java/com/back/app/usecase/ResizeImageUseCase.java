package com.back.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.utils.ImageUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${file.resize.max.width}")
    Integer maxWidth;

    public List<File> resizeMultipleImage(List<File> convertedImages) {
        return convertedImages.stream().map(f -> {
            try {
                return resizeImage(f);
            } catch (IOException e) {
                log.error("[ImageProcessingFailed] 이미지 리사이징 실패 : {}", e.getMessage());
                throw new CustomException(FailureCode.IMAGE_PROCESSING_FAILED);
            }
        }).toList();
    }

    public File resizeImage(File convertedImage) throws IOException {
        if (convertedImage == null || !convertedImage.exists()) {
            throw new IOException("리사이즈할 파일이 존재하지 않습니다.");
        }

        BufferedImage originalImage = ImageIO.read(convertedImage);
        int originWidth = originalImage.getWidth();
        int originHeight = originalImage.getHeight();

        if (originWidth < maxWidth) {
            return convertedImage;
        }

        double ratio = (double) originHeight / (double) originWidth;
        int resizedWidth = maxWidth;
        int resizedHeight = (int) Math.round(maxWidth * ratio);

        Image scaledImage = originalImage.getScaledInstance(resizedWidth, resizedHeight, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(resizedWidth, resizedHeight, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        File resizedFile = new File(System.getProperty("java.io.tmpdir") + "/resized_" + convertedImage.getName());

        // Ensure parent directory exists
        File parentDir = resizedFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Validate file extension, if it returns false, it means unsupported.
        // ImageUtility.validateFileExtension can also throw IOException
        // if fileName is null/empty or extension cannot be determined.
        boolean isValidExtension = imageUtility.validateFileExtension(resizedFile.getName());
        if (!isValidExtension) {
            // If validation returns false, it's an unsupported format, so throw an IOException
            throw new IOException("지원하지 않는 파일 형식입니다: " + resizedFile.getName());
        }

        String fileExtension = imageUtility.getFileExtension(resizedFile.getName());
        // At this point, fileExtension should not be null or empty due to earlier checks/exceptions.

        ImageIO.write(resizedImage, fileExtension, resizedFile);

        if (!resizedFile.exists()) {
            throw new IOException("리사이즈된 파일 생성에 실패했습니다.");
        }

        return resizedFile;
    }
}
