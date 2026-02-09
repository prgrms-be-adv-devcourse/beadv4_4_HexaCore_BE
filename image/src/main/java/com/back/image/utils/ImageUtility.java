package com.back.image.utils;

import com.back.image.config.FileExtensionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ImageUtility {
    private final FileExtensionProperties fileExtensionProperties;

    public String getFileExtension(String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("파일 이름이 null이거나 비어 있을 수 없습니다.");
        }

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new IOException("파일 확장자를 확인할 수 없습니다.");
        }

        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    public boolean validateFileExtension(String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("파일 이름을 확인할 수 없습니다.");
        }

        String fileExtension = getFileExtension(fileName);

        if (fileExtension.isEmpty()) {
            throw new IOException("파일 확장자를 확인할 수 없습니다.");
        }

        return fileExtensionProperties.getAllowed().stream()
                .anyMatch(fileExtension::equalsIgnoreCase);
    }
}
