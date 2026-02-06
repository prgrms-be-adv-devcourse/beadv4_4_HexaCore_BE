package com.back.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.utils.ImageUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvertImageUseCase {
    private final ImageUtility imageUtility;

    public List<File> convertMultipleFile(List<MultipartFile> multipartFiles) {
        return multipartFiles.stream().map(mf -> {
            try {
                return convertFile(mf);
            } catch (IOException e) {
                log.error("[ImageProcessingFailed] 이미지 파일 변환 실패 : {}", e.getMessage());
                throw new CustomException(FailureCode.IMAGE_PROCESSING_FAILED);
            }
        }).toList();
    }

    public File convertFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IOException("변환할 파일이 존재하지 않습니다.");
        }

        if (!imageUtility.validateFileExtension(multipartFile.getOriginalFilename())) {
            throw new IOException("지원하지 않는 파일 형식입니다.");
        }

        File originalTempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(originalTempFile)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new IOException("파일 업로드를 위한 임시 파일 생성에 실패했습니다.", e);
        }

        return originalTempFile;
    }
}
