package com.back.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class ImageUtility {

    @Value("${file.allowed.extensions}")
    private List<String> allowedExtensions;

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

        return allowedExtensions.stream().anyMatch(fileExtension::equalsIgnoreCase);
    }
}
